# Chat (AI Tutor) Improvements — Design Spec

**Date:** 2026-06-08
**Scope:** Prompts, UI, and Backend for `/chat` feature only
**Status:** Pending review

---

## 1. System Prompts — Teaching Protocol

### 1.1 Base Teaching Protocol

Every subject prompt in `SystemPrompts.java` gains a shared **Teaching Protocol** section that instructs the AI on *how* to teach, not just *what* it knows:

- **Start with a diagnostic**: 1-line check of what the student likely knows
- **Explain in 3 tiers**: simple → detailed → UPSC exam level
- **Use the Socratic method**: after explaining, pose a question that tests understanding
- **Include real-world Indian examples**: government schemes, current events, historical parallels
- **Highlight common mistakes**: what students typically get wrong
- **End with a practice question**: a "Try this" prompt for self-testing

### 1.2 Structured Output Format

Every prompt includes a strict output template (enforced in `PromptService`):

```markdown
## Concept
[Simple explanation in 2-3 sentences]

## Deep Dive
[Detailed breakdown with sub-headings as needed]
- Use bullet points, numbered lists
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
```

### 1.3 Subject Prompt Enhancements

Each subject prompt (`POLITY_SPECIALIST`, `HISTORY_SPECIALIST`, etc.) keeps its domain-specific content but appends:
1. The Teaching Protocol (shared across all)
2. The Structured Output Format (shared across all)

This is achieved by extracting the shared parts into constants and concatenating them in `getPromptForSubject()`.

### 1.4 Quiz/Evaluation Prompts (unchanged)

`SubjectPrompts.java` (quiz generation, answer evaluation, study plan prompts) is not modified in this scope.

---

## 2. Backend Improvements

### 2.1 Extended Context Window

**File:** `ChatService.java`

Change `getConversationHistory()` from last 10 messages to last 20. Simple parameter change.

### 2.2 Conversation Summarization

When a session exceeds 20 messages:
1. The first 10 messages are sent to the AI for summarization
2. The AI returns a 3-5 line summary of the key topics discussed
3. This summary is stored and prepended to subsequent prompts

**New DB table:** `chat_session_meta`
- `id` (PK, auto-generated)
- `session_id` (VARCHAR, unique)
- `user_id` (FK → users)
- `conversation_summary` (TEXT, nullable)
- `created_at`, `updated_at`

**New entity:** `ChatSessionMeta` in `model/entity/`
**New repository:** `ChatSessionMetaRepository`

**Flow in `ChatService.getConversationHistory()`:**
```
if messages > 20:
    if no summary exists → create one via AI call → store it
    return summary + last 20 messages
else:
    return all messages (up to 20 as before)
```

### 2.3 Structured Prompt Builder

Replace flat text prompt concatenation in `PromptService` with XML-tagged structure:

```
<system>{systemPrompt}</system>

<student_profile>
- Name: ...
- Level: ...
- Optional: ...
- Weak Areas: ...
</student_profile>

<conversation_summary>{summary}</conversation_summary>  <!-- if exists -->

<recent_conversation>{conversationHistory}</recent_conversation>

<response_format>{structured output template}</response_format>

<student>{userMessage}</student>
<mentor>:
```

XML-like tags are more robust than plain text because the model can distinguish between system instructions, conversation history, and the current user message.

### 2.4 Better Error Handling

**File:** `ChatService.chat()`

Current: Catches all exceptions and returns a single generic error message.

New: Specific error messages based on exception type:
- `TimeoutException` → "The AI model took too long. Try a shorter question or switch to a faster model."
- `HttpServerErrorException` (5xx) → "The AI service is temporarily unavailable. Please try again in a moment."
- `HttpClientErrorException` (4xx, rate limit) → "Too many requests. Please wait 30 seconds and try again."
- General `Exception` → "Sorry, I encountered an error. Please try again."

### 2.5 Files Modified (Backend)

| File | Change |
|------|--------|
| `SystemPrompts.java` | Add Teaching Protocol constant, restructure subject prompts |
| `PromptService.java` | XML-tagged prompt builder, structured output template |
| `ChatService.java` | 20-message context, summarization, error handling |
| `AiModelRouterService.java` | No changes |
| `ChatSessionMeta.java` | New entity |
| `ChatSessionMetaRepository.java` | New repository |
| `data.sql` | No seed changes needed |

---

## 3. UI Improvements

### 3.1 Markdown Rendering

Add `marked.js` (v15, CDN) to `chat.html` with Subresource Integrity:
```html
<script src="https://cdn.jsdelivr.net/npm/marked@15.0.0/marked.min.js"
        integrity="sha384-TBD5dG6M..."
        crossorigin="anonymous"></script>
```

In `chat.js`, render AI responses via `marked.parse(responseText)` instead of setting `.textContent`. The exact hash will be looked up from the npm package at build time.

### 3.2 Message Layout & Styling

**User messages:**
- Right-aligned
- Blue background (`#3B82F6`), white text
- Rounded corners, max-width 70%

**AI messages:**
- Left-aligned
- Light gray background (`#F3F4F6`), dark text
- Rounded corners, max-width 85%
- Copy button in bottom-right corner (📋 icon, appears on hover)

**Typing indicator:**
- Appears below last message during API call
- Shows: `⚡ UPSC Guru is thinking...`
- Animated dots (`...`)
- Hidden immediately when response arrives

### 3.3 Subject Selector

Add a subject dropdown above the chat input in `chat.html`:
```html
<select id="subjectSelector">
  <option value="GENERAL">General</option>
  <option value="POLITY">Polity</option>
  ...
</select>
```

JS populates options from `/api/user/optional-subjects` + hardcoded GS subjects. Selected subject is passed in the chat request body as `subject`.

### 3.4 Auto-scroll

After appending a new message (user or AI), scroll the chat container to the bottom:
```js
chatContainer.scrollTop = chatContainer.scrollHeight;
```

### 3.5 Copy Button

Each AI message gets a small copy button. On click:
```js
navigator.clipboard.writeText(plainTextContent);
// Show brief "Copied!" tooltip
```

The plain text is extracted from the markdown content (strip markdown for clipboard, or use `innerText` of the rendered HTML).

### 3.6 Mobile Responsiveness

- Message bubbles: max-width 90% on screens < 768px
- Input bar: fixed at bottom with `position: sticky; bottom: 0`
- Subject selector: full-width on mobile
- Sidebar: collapses to hamburger menu (already exists, just ensure it works)

### 3.7 Cache Bump

Update the script version in `chat.html` to force browser reload:
```html
<script th:src="@{/js/chat.js(v='20260608-chat-v2')}"></script>
```

### 3.8 Files Modified (Frontend)

| File | Change |
|------|--------|
| `chat.html` | Add marked.js CDN, subject selector, copy button templates, cache bump |
| `chat.js` | Markdown rendering, typing indicator, auto-scroll, copy, subject selector logic |
| `style.css` | Message bubble styles, typing indicator animation, mobile responsive rules |

---

## 4. Data Flow

```
User types message + selects subject
       │
       ▼
chat.js: POST /api/chat/send {userId, message, subject, sessionId}
       │
       ▼
ChatController → ChatService.chat()
       │
       ├─ getConversationHistory() → last 20 msgs + summary if >20
       ├─ PromptService.buildSubjectPrompt() → XML-tagged prompt
       ├─ AiModelRouterService.generate() → LLM call
       └─ saveChatHistory() → persist messages
       │
       ▼
ChatResponse JSON → chat.js
       │
       ├─ Render user message bubble (right)
       ├─ Render AI response via marked.parse() (left)
       ├─ Add copy button to AI message
       └─ Auto-scroll to bottom
```

---

## 5. Error Handling & Edge Cases

| Scenario | Handling |
|----------|----------|
| AI times out | Show user-friendly timeout message, suggest shorter question |
| AI returns empty response | Show "No response received. Try rephrasing." |
| AI response is not valid markdown | marked.js handles gracefully — renders as plain text |
| User sends empty message | JS validation blocks submit |
| Very long session (>200 messages) | Summarization condenses early content; last 20 always fresh |
| No Ollama running + no API key | Error message explains how to configure AI model |
| Network disconnect during API call | Catch and show "Connection lost. Please check your internet." |

---

## 6. What This Does NOT Change

- Quiz generation/evaluation prompts (separate scope)
- Study plan, PYQ analysis, current affairs features
- Database schema for existing tables (only adds `chat_session_meta`)
- Auth/registration flow
- Dashboard or progress tracking
- Other controllers/services
