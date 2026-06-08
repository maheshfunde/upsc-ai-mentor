package com.upscmentor.service;

import com.upscmentor.model.dto.ChatRequest;
import com.upscmentor.model.dto.ChatResponse;
import com.upscmentor.model.entity.ChatHistory;
import com.upscmentor.model.entity.ChatSessionMeta;
import com.upscmentor.model.entity.User;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.repository.ChatHistoryRepository;
import com.upscmentor.repository.ChatSessionMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    // Keep last 20 messages in context for coherent conversation
    private static final int CONTEXT_WINDOW = 20;

    // Trigger summarization when history exceeds 25 messages
    // (provides 5-message buffer before context window fills)
    private static final int SUMMARIZE_THRESHOLD = 25;

    // After summarization, retain 15 most recent messages alongside summary
    private static final int SUMMARIZE_KEEP = 15;

    private final AiModelRouterService aiModelRouterService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserService userService;
    private final PromptService promptService;
    private final ChatSessionMetaRepository chatSessionMetaRepository;

    public ChatService(AiModelRouterService aiModelRouterService,
                       ChatHistoryRepository chatHistoryRepository,
                       UserService userService,
                       PromptService promptService,
                       ChatSessionMetaRepository chatSessionMetaRepository) {
        this.aiModelRouterService = aiModelRouterService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.userService = userService;
        this.promptService = promptService;
        this.chatSessionMetaRepository = chatSessionMetaRepository;
    }

    /**
     * Process a chat message with subject-specific AI tutoring
     */
    public ChatResponse chat(ChatRequest request) {
        try {
            // Get user profile
            User user = userService.getUserById(request.getUserId());

            // Get or create session
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            List<ChatHistory> fullHistory = chatHistoryRepository
                    .findBySessionIdOrderByCreatedAtAsc(sessionId);
            String conversationHistory = getConversationHistory(sessionId);
            String conversationSummary = getConversationSummaryText(sessionId, fullHistory);

            PromptResult promptResult = buildFullPrompt(request, user, conversationSummary, conversationHistory);
            String fullPrompt = promptResult.prompt();
            String subjectName = promptResult.subjectName();

            logger.info("Processing chat for user: {}, subject: {}, session: {}",
                    user.getUsername(), subjectName, sessionId);

            // Save user message to history
            saveChatHistory(user.getId(), sessionId,
                    request.getSubject(), "USER", request.getMessage());

            // Call AI model
            String aiResponse = aiModelRouterService.generate(user, fullPrompt, request.getLocalModelName());

            // Save AI response to history
            saveChatHistory(user.getId(), sessionId,
                    request.getSubject(), "ASSISTANT", aiResponse);

            // Update user activity
            userService.updateLastActive(user.getId());

            logger.info("Successfully generated response for session: {}", sessionId);

            return ChatResponse.success(aiResponse, sessionId, subjectName);

        } catch (Exception e) {
            String userMessage = classifyChatError(e);
            logger.error("Error in chat service: {}", e.getMessage(), e);
            return ChatResponse.error(userMessage);
        }
    }

    /**
     * Get conversation history for a session.
     * When messages exceed SUMMARIZE_THRESHOLD, earlier messages are condensed
     * into a summary and only the most recent CONTEXT_WINDOW messages are included.
     */
    private String getConversationHistory(String sessionId) {
        List<ChatHistory> history = chatHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (history.isEmpty()) return "";

        // Trigger summarization if needed
        getOrCreateConversationSummary(sessionId, history);

        // Take last CONTEXT_WINDOW messages
        int start = Math.max(0, history.size() - CONTEXT_WINDOW);
        return history.subList(start, history.size()).stream()
                .map(msg -> {
                    String role = msg.getRole().equals("USER") ? "Student" : "Mentor";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get the stored conversation summary text for a session (if any).
     */
    private String getConversationSummaryText(String sessionId, List<ChatHistory> history) {
        if (history.size() < SUMMARIZE_THRESHOLD) {
            return null;
        }
        Optional<ChatSessionMeta> meta = chatSessionMetaRepository.findBySessionId(sessionId);
        return meta.map(ChatSessionMeta::getConversationSummary).orElse(null);
    }

    /**
     * Create or retrieve conversation summary for long sessions.
     */
    private String getOrCreateConversationSummary(String sessionId, List<ChatHistory> history) {
        if (history.size() < SUMMARIZE_THRESHOLD) {
            return null;
        }

        Optional<ChatSessionMeta> existing = chatSessionMetaRepository.findBySessionId(sessionId);
        if (existing.isPresent() && existing.get().getConversationSummary() != null) {
            return existing.get().getConversationSummary();
        }

        // Create summary from early messages (those NOT in the context window)
        int summaryEnd = Math.max(0, history.size() - SUMMARIZE_KEEP);
        if (summaryEnd == 0) return null;

        String messagesToSummarize = history.subList(0, summaryEnd).stream()
                .map(msg -> {
                    String role = msg.getRole().equals("USER") ? "Student" : "Mentor";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));

        String summaryPrompt = "Summarize the following UPSC tutoring conversation in 3-5 lines. " +
                "Focus on key topics discussed and any learning gaps identified. " +
                "Keep it concise:\n\n" + messagesToSummarize;

        try {
            User systemUser = new User();
            systemUser.setUsername("system");
            String summary = aiModelRouterService.generate(systemUser, summaryPrompt);

            ChatSessionMeta meta = new ChatSessionMeta();
            meta.setSessionId(sessionId);
            meta.setConversationSummary(summary);
            if (!history.isEmpty()) {
                meta.setUserId(history.get(0).getUserId());
            }
            chatSessionMetaRepository.save(meta);

            return summary;
        } catch (Exception e) {
            logger.error("Failed to generate conversation summary for session {}: {}", sessionId, e.getMessage());
            return null;
        }
    }

    /**
     * Classify an exception into a user-friendly error message.
     */
    private String classifyChatError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

        if (msg.contains("timeout") || msg.contains("timed out") || e instanceof java.util.concurrent.TimeoutException) {
            return "The AI model took too long to respond. Try a shorter question, or switch to a faster model in AI Settings.";
        }

        if (msg.contains("429") || msg.contains("rate limit") || msg.contains("too many requests")) {
            return "Too many requests. Please wait 30 seconds and try again.";
        }

        if (msg.contains("connection") || msg.contains("refused") || msg.contains("unreachable")) {
            return "Cannot connect to the AI model. If using Ollama, make sure it's running on http://localhost:11434. Otherwise, check your API key in AI Settings.";
        }

        if ((msg.contains("5") && msg.contains("status")) || msg.contains("server error")) {
            return "The AI service is temporarily unavailable. Please try again in a moment.";
        }

        return "Sorry, I encountered an error. Please try again. Error: " + e.getMessage();
    }

    /**
     * Save a chat message to database
     */
    private void saveChatHistory(Long userId, String sessionId,
                                 Subject subject, String role, String content) {
        ChatHistory chatHistory = new ChatHistory(userId, sessionId, subject, role, content);
        chatHistoryRepository.save(chatHistory);
    }

    /**
     * Get all chat sessions for a user
     */
    public List<ChatHistory> getUserChatHistory(Long userId) {
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get chat history for specific subject
     */
    public List<ChatHistory> getUserSubjectHistory(Long userId, Subject subject) {
        return chatHistoryRepository.findByUserIdAndSubjectOrderByCreatedAtDesc(userId, subject);
    }

    /**
     * Get chat history for a specific user session
     */
    public List<ChatHistory> getSessionHistory(Long userId, String sessionId) {
        return chatHistoryRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }

    /**
     * Get session summaries for old-chat selector
     */
    public List<Map<String, Object>> getSessionSummaries(Long userId) {
        List<ChatHistory> all = chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, Map<String, Object>> summaries = new LinkedHashMap<>();

        for (ChatHistory item : all) {
            String sid = item.getSessionId();
            Map<String, Object> summary = summaries.computeIfAbsent(sid, k -> {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("sessionId", sid);
                s.put("lastMessage", item.getContent());
                s.put("updatedAt", item.getCreatedAt());
                s.put("subject", item.getSubject() != null ? item.getSubject().name() : "GENERAL");
                s.put("messageCount", 0);
                return s;
            });
            int count = (int) summary.get("messageCount");
            summary.put("messageCount", count + 1);
        }

        return summaries.values().stream().limit(50).collect(Collectors.toList());
    }

    /**
     * Create a new session ID
     */
    public String createNewSession() {
        return UUID.randomUUID().toString();
    }

    /**
     * Build the complete prompt for AI generation based on subject type.
     */
    private PromptResult buildFullPrompt(ChatRequest request, User user,
                                         String conversationSummary,
                                         String conversationHistory) {
        String fullPrompt;
        String subjectName;
        String language = request.getResponseLanguage() != null ? request.getResponseLanguage() : "en";

        if (request.isOptionalSubject()) {
            fullPrompt = promptService.buildOptionalSubjectPrompt(
                    user, conversationSummary, conversationHistory, request.getMessage(), language);
            subjectName = user.getOptionalSubject() != null
                    ? user.getOptionalSubject().getDisplayName()
                    : "Optional Subject";
        } else {
            Subject subject = request.getSubject() != null
                    ? request.getSubject()
                    : Subject.GENERAL;
            fullPrompt = promptService.buildSubjectPrompt(
                    user, subject, conversationSummary, conversationHistory, request.getMessage(), language);
            subjectName = subject.getDisplayName();
        }

        return new PromptResult(fullPrompt, subjectName);
    }

    /**
     * Simple record to hold prompt + subject name together.
     */
    private record PromptResult(String prompt, String subjectName) {}
}
