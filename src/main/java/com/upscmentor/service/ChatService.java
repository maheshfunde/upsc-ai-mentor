package com.upscmentor.service;

import com.upscmentor.model.dto.ChatRequest;
import com.upscmentor.model.dto.ChatResponse;
import com.upscmentor.model.entity.ChatHistory;
import com.upscmentor.model.entity.User;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.repository.ChatHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final AiModelRouterService aiModelRouterService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserService userService;
    private final PromptService promptService;

    public ChatService(AiModelRouterService aiModelRouterService,
                       ChatHistoryRepository chatHistoryRepository,
                       UserService userService,
                       PromptService promptService) {
        this.aiModelRouterService = aiModelRouterService;
        this.chatHistoryRepository = chatHistoryRepository;
        this.userService = userService;
        this.promptService = promptService;
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

            // Get conversation history for context
            String conversationHistory = getConversationHistory(sessionId);

            // Build the appropriate prompt based on subject type
            String fullPrompt;
            String subjectName;

            if (request.isOptionalSubject()) {
                // Use optional subject prompt
                fullPrompt = promptService.buildOptionalSubjectPrompt(
                        user, conversationHistory, request.getMessage());
                subjectName = user.getOptionalSubject().getDisplayName();
            } else {
                // Use specific GS subject prompt
                Subject subject = request.getSubject() != null
                        ? request.getSubject()
                        : Subject.GENERAL;
                fullPrompt = promptService.buildSubjectPrompt(
                        user, subject, conversationHistory, request.getMessage());
                subjectName = subject.getDisplayName();
            }

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
            logger.error("Error in chat service: {}", e.getMessage(), e);
            return ChatResponse.error(
                    "Sorry, I encountered an error. Please try again. " +
                            "Check your AI model configuration. Error: " + e.getMessage());
        }
    }

    /**
     * Get conversation history for a session (last 10 messages)
     */
    private String getConversationHistory(String sessionId) {
        List<ChatHistory> history = chatHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (history.isEmpty()) return "";

        // Take last 10 messages for context window management
        int start = Math.max(0, history.size() - 10);
        return history.subList(start, history.size()).stream()
                .map(msg -> {
                    String role = msg.getRole().equals("USER") ? "Student" : "Mentor";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));
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
}
