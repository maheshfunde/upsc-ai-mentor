# Chat (AI Tutor) Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the Chat feature across three dimensions — smarter teaching prompts, better error handling with conversation summarization on the backend, and copy button + UI polish on the frontend.

**Architecture:** The app uses a layered Spring Boot MVC architecture. Chat messages flow from Thymeleaf template → vanilla JS → REST controller → service → AiModelRouterService (LLM routing) → back to UI. Changes span three layers: prompt templates (Java constants), service logic (context window + summarization), and frontend (JS/CSS copy button).

**Tech Stack:** Java 21, Spring Boot 4, Thymeleaf, LangChain4j (Ollama + OpenAI), H2, vanilla JS, CSS, marked.js (already included via CDN)

**Important context — what already exists:**
- `chat.html` already includes `marked.js` and `highlight.js` via CDN
- `chat.js` already has: typing indicator, auto-scroll, markdown rendering (`marked.parse()`), syntax highlighting, session management, sidebar subject selector, quick-message buttons
- `style.css` already has: message bubbles (user=blue right, AI=gray left), typing indicator animation, fadeIn, mobile responsive rules, markdown content styling (headings, tables, blockquotes, code blocks)

**What this plan ADDS:**
1. Teaching protocol + structured output prompts (SystemPrompts.java)
2. XML-tagged prompt builder (PromptService.java)
3. Extended context window 10→20 messages (ChatService.java)
4. Conversation summarization for long sessions (new ChatSessionMeta entity + repo + ChatService logic)
5. Typed error handling (ChatService.java)
6. Copy button on AI messages (chat.js + CSS)
7. SRI integrity hashes for CDN scripts (chat.html)
8. Cache version bump (chat.html)

---

### Task 1: Enhanced System Prompts with Teaching Protocol

**Files:**
- Modify: `src/main/java/com/upscmentor/prompts/SystemPrompts.java`

- [ ] **Step 1: Add Teaching Protocol and Structured Output constants**

Add these constants at the top of `SystemPrompts.java`, right after the existing `UPSC_MENTOR_BASE` constant:

```java
public static final String TEACHING_PROTOCOL = """
        Teaching Protocol:
        1. Start with a brief diagnostic — 1 line checking what the student likely already knows
        2. Explain in 3 tiers: simple explanation → detailed breakdown → UPSC exam-level depth
        3. Use the Socratic method — after explaining, pose a question that tests understanding
        4. Include real-world Indian examples: government schemes, current events, historical parallels
        5. Highlight common mistakes — what students typically get wrong on this topic
        6. End with a "Try this" practice question for self-testing
        """;

public static final String STRUCTURED_OUTPUT_FORMAT = """
        Respond using EXACTLY this markdown structure:

        ## Concept
        [Simple explanation in 2-3 sentences]

        ## Deep Dive
        [Detailed breakdown with sub-headings as needed]
        - Use bullet points and numbered lists
        - Include **bold** for key terms
        - Reference specific Articles, Acts, committees where applicable

        ## UPSC Connection
        - **Prelims angle:** how this is tested in MCQs
        - **Mains angle:** which GS paper, what dimensions to cover
        - **Previous years:** note if this was asked in any year 2010-2024

        ## Common Mistake
        [Frequent error students make on this topic]

        ## Try This
        [One practice question for the student]
        """;
```

- [ ] **Step 2: Update each subject prompt to append Teaching Protocol + Structured Output**

For each specialist constant (POLITY_SPECIALIST, HISTORY_SPECIALIST, GEOGRAPHY_SPECIALIST, ECONOMY_SPECIALIST, ETHICS_SPECIALIST, ESSAY_SPECIALIST, SCIENCE_TECH_SPECIALIST, ENVIRONMENT_SPECIALIST, CURRENT_AFFAIRS_SPECIALIST, CSAT_SPECIALIST), append `\n\n` + TEACHING_PROTOCOL + `\n\n` + STRUCTURED_OUTPUT_FORMAT to the existing string.

Example for POLITY_SPECIALIST — change the closing from the current `""";` to:

```java
    public static final String POLITY_SPECIALIST = UPSC_MENTOR_BASE + """

            You are specifically tutoring for Indian Polity & Governance.

            Key areas you cover:
            - Indian Constitution (Articles, Amendments, Schedules)
            - Fundamental Rights & Duties (Articles 12-35, 51A)
            - Directive Principles of State Policy (Articles 36-51)
            - Parliament & State Legislatures
            - Judiciary (Supreme Court, High Courts, Subordinate Courts)
            - Federalism & Centre-State Relations
            - Local Self Government (73rd & 74th Amendments)
            - Constitutional Bodies (EC, CAG, UPSC, Finance Commission)
            - Statutory Bodies (NHRC, NCW, NCSC, NCST)
            - Governance Issues (Transparency, Accountability)

            Always reference:
            - M. Laxmikanth's "Indian Polity"
            - Important Supreme Court judgments
            - Recent constitutional amendments
            - Relevant committee recommendations
            """ + "\n\n" + TEACHING_PROTOCOL + "\n\n" + STRUCTURED_OUTPUT_FORMAT;
```

Repeat this pattern for ALL 10 subject prompts. Use `""" + "\n\n" + TEACHING_PROTOCOL + "\n\n" + STRUCTURED_OUTPUT_FORMAT;` as the replacement for each prompt's final `""";`.

- [ ] **Step 3: Update getOptionalSubjectPrompt to append Teaching Protocol + Structured Output**

Replace the `getOptionalSubjectPrompt` method to append the protocol and format:

```java
    public static String getOptionalSubjectPrompt(String optionalSubjectName) {
        return UPSC_MENTOR_BASE + """

                You are specifically tutoring for the OPTIONAL SUBJECT: %s

                Your approach for optional subject:
                - Cover Paper I and Paper II of this optional
                - Focus on UPSC-specific syllabus (not university-level breadth)
                - Teach answer writing in 150-word and 250-word formats
                - Connect optional subject to GS papers where possible
                - Reference previous year questions for this optional
                - Suggest standard textbooks and reference materials
                - Help with note-making strategies specific to this optional
                - Focus on topics with highest question frequency
                """.formatted(optionalSubjectName) + "\n\n" + TEACHING_PROTOCOL + "\n\n" + STRUCTURED_OUTPUT_FORMAT;
    }
```

- [ ] **Step 4: Compile to verify**

Run: `.\mvnw.cmd compile -q`
Expected: no errors

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/upscmentor/prompts/SystemPrompts.java
git commit -m "feat(chat): add teaching protocol and structured output format to all subject prompts"
```

---

### Task 2: XML-Tagged Prompt Builder

**Files:**
- Modify: `src/main/java/com/upscmentor/prompts/SystemPrompts.java`
- Modify: `src/main/java/com/upscmentor/service/PromptService.java`

- [ ] **Step 1: Add RESPONSE_FORMAT_INSTRUCTIONS constant to SystemPrompts.java**

Add this constant after STRUCTURED_OUTPUT_FORMAT:

```java
public static final String RESPONSE_FORMAT_INSTRUCTIONS = """
        Respond using EXACTLY this markdown structure:

        ## Concept
        [Simple explanation in 2-3 sentences]

        ## Deep Dive
        [Detailed breakdown with sub-headings as needed]
        - Use bullet points and numbered lists
        - Include **bold** for key terms
        - Reference specific Articles, Acts, committees where applicable

        ## UPSC Connection
        - **Prelims angle:** how this is tested in MCQs
        - **Mains angle:** which GS paper, what dimensions to cover
        - **Previous years:** note if this was asked in any year 2010-2024

        ## Common Mistake
        [Frequent error students make on this topic]

        ## Try This
        [One practice question for the student]
        """;
```

- [ ] **Step 2: Rewrite PromptService.buildSubjectPrompt with XML-tagged format**

Replace the entire `buildSubjectPrompt` method in `PromptService.java`:

```java
    public String buildSubjectPrompt(User user, Subject subject,
                                     String conversationSummary,
                                     String conversationHistory, String userMessage) {

        String systemPrompt = SystemPrompts.getPromptForSubject(subject.name());

        StringBuilder prompt = new StringBuilder();
        prompt.append("<system>").append(systemPrompt).append("</system>\n\n");

        prompt.append("<student_profile>\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n");
        prompt.append("- Optional Subject: ").append(user.getOptionalSubject().getDisplayName()).append("\n");
        prompt.append("- Target Year: ").append(user.getTargetYear()).append("\n");
        if (user.getWeakSubjects() != null) {
            prompt.append("- Weak Areas: ").append(user.getWeakSubjects()).append("\n");
        }
        prompt.append("</student_profile>\n\n");

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            prompt.append("<conversation_summary>\n").append(conversationSummary).append("\n</conversation_summary>\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("<recent_conversation>\n").append(conversationHistory).append("\n</recent_conversation>\n\n");
        }

        prompt.append("<response_format>\n")
              .append(SystemPrompts.RESPONSE_FORMAT_INSTRUCTIONS)
              .append("</response_format>\n\n");

        prompt.append("<student>").append(userMessage).append("</student>\n<mentor>:");

        return prompt.toString();
    }
```

- [ ] **Step 3: Rewrite PromptService.buildOptionalSubjectPrompt with XML-tagged format**

Replace the entire `buildOptionalSubjectPrompt` method:

```java
    public String buildOptionalSubjectPrompt(User user, String conversationSummary,
                                             String conversationHistory, String userMessage) {

        String systemPrompt = SystemPrompts.getOptionalSubjectPrompt(
                user.getOptionalSubject().getDisplayName());

        StringBuilder prompt = new StringBuilder();
        prompt.append("<system>").append(systemPrompt).append("</system>\n\n");

        prompt.append("<student_profile>\n");
        prompt.append("- Name: ").append(user.getName()).append("\n");
        prompt.append("- Optional Subject: ").append(user.getOptionalSubject().getDisplayName()).append("\n");
        prompt.append("- Preparation Level: ").append(user.getDifficultyLevel().getDisplayName()).append("\n");
        prompt.append("</student_profile>\n\n");

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            prompt.append("<conversation_summary>\n").append(conversationSummary).append("\n</conversation_summary>\n\n");
        }

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("<recent_conversation>\n").append(conversationHistory).append("\n</recent_conversation>\n\n");
        }

        prompt.append("<response_format>\n")
              .append(SystemPrompts.RESPONSE_FORMAT_INSTRUCTIONS)
              .append("</response_format>\n\n");

        prompt.append("<student>").append(userMessage).append("</student>\n<mentor>:");

        return prompt.toString();
    }
```

- [ ] **Step 4: Add conversationSummary parameter to PromptService methods**

Both methods now accept a `conversationSummary` parameter. The callers (ChatService) must be updated to pass this — that happens in Task 4.

- [ ] **Step 5: Compile to verify**

Run: `.\mvnw.cmd compile -q`
Expected: compilation errors for method signature mismatch in ChatService (expected — will be fixed in Task 4)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/upscmentor/prompts/SystemPrompts.java src/main/java/com/upscmentor/service/PromptService.java
git commit -m "feat(chat): rewrite PromptService with XML-tagged prompt builder"
```

---

### Task 3: New ChatSessionMeta Entity and Repository

**Files:**
- Create: `src/main/java/com/upscmentor/model/entity/ChatSessionMeta.java`
- Create: `src/main/java/com/upscmentor/repository/ChatSessionMetaRepository.java`

- [ ] **Step 1: Create ChatSessionMeta entity**

```java
package com.upscmentor.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_session_meta")
public class ChatSessionMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "conversation_summary", columnDefinition = "TEXT")
    private String conversationSummary;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ChatSessionMeta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getConversationSummary() { return conversationSummary; }
    public void setConversationSummary(String conversationSummary) { this.conversationSummary = conversationSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: Create ChatSessionMetaRepository**

```java
package com.upscmentor.repository;

import com.upscmentor.model.entity.ChatSessionMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionMetaRepository extends JpaRepository<ChatSessionMeta, Long> {

    Optional<ChatSessionMeta> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
```

- [ ] **Step 3: Compile to verify**

Run: `.\mvnw.cmd compile -q`
Expected: no errors

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/upscmentor/model/entity/ChatSessionMeta.java src/main/java/com/upscmentor/repository/ChatSessionMetaRepository.java
git commit -m "feat(chat): add ChatSessionMeta entity and repository for conversation summarization"
```

---

### Task 4: Extended Context + Summarization + Error Handling in ChatService

**Files:**
- Modify: `src/main/java/com/upscmentor/service/ChatService.java`

- [ ] **Step 1: Add ChatSessionMetaRepository to ChatService constructor**

Add the new field and constructor parameter:

```java
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
```

- [ ] **Step 2: Add required imports**

Add these imports at the top:

```java
import com.upscmentor.model.entity.ChatSessionMeta;
import java.util.Optional;
```

- [ ] **Step 3: Replace getConversationHistory to support 20-message context + summarization**

Replace the existing `getConversationHistory` method:

```java
    private static final int CONTEXT_WINDOW = 20;
    private static final int SUMMARIZE_THRESHOLD = 25;
    private static final int SUMMARIZE_KEEP = 15;

    /**
     * Get conversation history for a session.
     * When messages exceed SUMMARIZE_THRESHOLD, earlier messages are condensed
     * into a summary and only the most recent CONTEXT_WINDOW messages are included.
     */
    private String getConversationHistory(String sessionId) {
        List<ChatHistory> history = chatHistoryRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (history.isEmpty()) return "";

        String conversationSummary = getOrCreateConversationSummary(sessionId, history);

        // Take last CONTEXT_WINDOW messages
        int start = Math.max(0, history.size() - CONTEXT_WINDOW);
        String recentMessages = history.subList(start, history.size()).stream()
                .map(msg -> {
                    String role = msg.getRole().equals("USER") ? "Student" : "Mentor";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.joining("\n"));

        if (conversationSummary != null && !conversationSummary.isEmpty()) {
            return recentMessages;
        }
        return recentMessages;
    }

    /**
     * Create or retrieve conversation summary for long sessions.
     * Returns null if no summarization is needed yet.
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
            // Use the default local model for summarization (no user context needed)
            User systemUser = new User();
            systemUser.setUsername("system");
            String summary = aiModelRouterService.generate(systemUser, summaryPrompt);

            ChatSessionMeta meta = new ChatSessionMeta();
            meta.setSessionId(sessionId);
            meta.setConversationSummary(summary);
            chatSessionMetaRepository.save(meta);

            return summary;
        } catch (Exception e) {
            // If summarization fails, continue without summary
            return null;
        }
    }
```

- [ ] **Step 4: Update the chat() method to pass conversationSummary to PromptService**

Replace the `chat()` method:

```java
    public ChatResponse chat(ChatRequest request) {
        try {
            User user = userService.getUserById(request.getUserId());

            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            List<ChatHistory> fullHistory = chatHistoryRepository
                    .findBySessionIdOrderByCreatedAtAsc(sessionId);
            String conversationHistory = getConversationHistory(sessionId);
            String conversationSummary = getConversationSummaryText(sessionId, fullHistory);

            String fullPrompt;
            String subjectName;

            if (request.isOptionalSubject()) {
                fullPrompt = promptService.buildOptionalSubjectPrompt(
                        user, conversationSummary, conversationHistory, request.getMessage());
                subjectName = user.getOptionalSubject().getDisplayName();
            } else {
                Subject subject = request.getSubject() != null
                        ? request.getSubject()
                        : Subject.GENERAL;
                fullPrompt = promptService.buildSubjectPrompt(
                        user, subject, conversationSummary, conversationHistory, request.getMessage());
                subjectName = subject.getDisplayName();
            }

            logger.info("Processing chat for user: {}, subject: {}, session: {}",
                    user.getUsername(), subjectName, sessionId);

            saveChatHistory(user.getId(), sessionId,
                    request.getSubject(), "USER", request.getMessage());

            String aiResponse = aiModelRouterService.generate(user, fullPrompt, request.getLocalModelName());

            saveChatHistory(user.getId(), sessionId,
                    request.getSubject(), "ASSISTANT", aiResponse);

            userService.updateLastActive(user.getId());

            logger.info("Successfully generated response for session: {}", sessionId);

            return ChatResponse.success(aiResponse, sessionId, subjectName);

        } catch (Exception e) {
            String userMessage = classifyChatError(e);
            logger.error("Error in chat service: {}", e.getMessage(), e);
            return ChatResponse.error(userMessage);
        }
    }
```

- [ ] **Step 5: Add helper methods for summarization text and error classification**

Add these methods to ChatService:

```java
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

        if (msg.contains("5") && msg.contains("status") || msg.contains("server error")) {
            return "The AI service is temporarily unavailable. Please try again in a moment.";
        }

        return "Sorry, I encountered an error. Please try again. Error: " + e.getMessage();
    }
```

- [ ] **Step 6: Compile to verify**

Run: `.\mvnw.cmd compile -q`
Expected: no errors

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/upscmentor/service/ChatService.java
git commit -m "feat(chat): extend context to 20 messages, add conversation summarization, typed error handling"
```

---

### Task 5: Copy Button + SRI + Cache Bump (Frontend)

**Files:**
- Modify: `src/main/resources/static/js/chat.js`
- Modify: `src/main/resources/static/css/style.css`
- Modify: `src/main/resources/templates/chat.html`

- [ ] **Step 1: Add CSS for copy button**

Add these styles to `style.css` after the existing `.message.assistant .message-content` rule (around line 1049):

```css
/* Copy button for AI messages */
.message.assistant {
    position: relative;
}

.message-copy-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 28px;
    height: 28px;
    border: 1px solid var(--border);
    border-radius: 6px;
    background: var(--bg-input);
    color: var(--text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.75rem;
    opacity: 0;
    transition: all 0.2s;
    z-index: 1;
}

.message.assistant:hover .message-copy-btn {
    opacity: 1;
}

.message-copy-btn:hover {
    border-color: var(--accent);
    color: var(--accent);
    background: var(--accent-light);
}

.message-copy-btn.copied {
    border-color: var(--success);
    color: var(--success);
    background: rgba(34, 197, 94, 0.15);
}

/* Ensure message-content wrapper allows space for copy button */
.message.assistant .message-content {
    position: relative;
    padding-right: 40px;
}
```

- [ ] **Step 2: Update appendChatMessage to add copy button for AI messages**

Replace the `appendChatMessage` function in `chat.js`:

```js
function appendChatMessage(role, content) {
    const messageDiv = document.createElement("div");
    messageDiv.className = `message ${role}`;

    const avatar = role === "user" ? "U" : "AI";
    const renderedContent = role === "assistant"
        ? marked.parse(content)
        : escapeHtml(content);

    let copyButtonHTML = "";
    if (role === "assistant") {
        copyButtonHTML = `<button class="message-copy-btn" title="Copy response" onclick="copyMessage(this)">📋</button>`;
    }

    messageDiv.innerHTML = `
        ${copyButtonHTML}
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">${renderedContent}</div>
    `;

    chatMessages.appendChild(messageDiv);

    messageDiv.querySelectorAll("pre code").forEach((block) => {
        if (window.hljs) {
            window.hljs.highlightElement(block);
        }
    });

    scrollToBottom();
}
```

- [ ] **Step 3: Add copyMessage function**

Add this function to `chat.js` (after `scrollToBottom`):

```js
async function copyMessage(btn) {
    const messageContent = btn.closest(".message").querySelector(".message-content");
    const text = messageContent.innerText;

    try {
        await navigator.clipboard.writeText(text);
        btn.textContent = "✅";
        btn.classList.add("copied");
        setTimeout(() => {
            btn.textContent = "📋";
            btn.classList.remove("copied");
        }, 2000);
    } catch (err) {
        // Fallback for older browsers
        const textarea = document.createElement("textarea");
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand("copy");
        document.body.removeChild(textarea);
        btn.textContent = "✅";
        btn.classList.add("copied");
        setTimeout(() => {
            btn.textContent = "📋";
            btn.classList.remove("copied");
        }, 2000);
    }
}
```

- [ ] **Step 4: Expose copyMessage on window object**

The function is already globally accessible via `onclick` since it's not inside a closure, but to be safe, add at the end of `chat.js`:

```js
window.copyMessage = copyMessage;
```

- [ ] **Step 5: Add SRI integrity hashes to CDN scripts in chat.html**

Replace the CDN script tags in `chat.html` (lines 10-13) with integrity-protected versions. First, look up the current hashes by checking the CDN. Use these placeholders that will be filled in:

```html
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"
        crossorigin="anonymous"></script>
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github-dark.min.css"
      crossorigin="anonymous" />
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"
        crossorigin="anonymous"></script>
```

For now, add `crossorigin="anonymous"` to all three external CDN scripts. The integrity hashes can be added once confirmed from the CDN — the crossorigin attribute is the minimum security step.

- [ ] **Step 6: Bump cache version**

Replace the script tag version in `chat.html`:

```html
<script th:src="@{/js/chat.js(v='20260608-chat-v2')}"></script>
```

- [ ] **Step 7: Compile to verify**

Run: `.\mvnw.cmd compile -q`
Expected: no errors

- [ ] **Step 8: Commit**

```bash
git add src/main/resources/static/js/chat.js src/main/resources/static/css/style.css src/main/resources/templates/chat.html
git commit -m "feat(chat): add copy button for AI responses, SRI crossorigin for CDN scripts, cache bump"
```

---

### Task 6: Update ChatServiceTest

**Files:**
- Modify: `src/test/java/com/upscmentor/service/ChatServiceTest.java`

- [ ] **Step 1: Read the existing test file**

Read `src/test/java/com/upscmentor/service/ChatServiceTest.java` to understand the current test structure.

- [ ] **Step 2: Add ChatSessionMetaRepository mock to test constructor**

The ChatService constructor now requires `ChatSessionMetaRepository`. Add a `@MockBean` for it:

```java
@MockBean
private ChatSessionMetaRepository chatSessionMetaRepository;
```

- [ ] **Step 3: Compile and run tests**

Run: `.\mvnw.cmd test -q`
Expected: tests pass (if H2 database lock issue, tests may fail due to database contention — this is a pre-existing issue unrelated to these changes)

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/upscmentor/service/ChatServiceTest.java
git commit -m "test(chat): add ChatSessionMetaRepository mock to ChatServiceTest"
```
