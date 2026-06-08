// ============================================
// UPSC AI MENTOR - CHAT LOGIC
// ============================================

let sessionId = null;
let currentSubject = "GENERAL";
let isOptionalMode = false;
let currentUser = null;

const chatMessages = document.getElementById("chatMessages");
const messageInput = document.getElementById("messageInput");
const sendBtn = document.getElementById("sendBtn");
const oldChatsSelect = document.getElementById("oldChatsSelect");

document.addEventListener("DOMContentLoaded", () => {
    initChat();
    setupChatEventListeners();
});

async function initChat() {
    currentUser = JSON.parse(localStorage.getItem("upscMentorUser"));
    if (!currentUser) {
        window.location.href = "/onboarding";
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const subjectParam = params.get("subject");
    const optionalParam = params.get("optional");

    if (optionalParam === "true") {
        await switchSubject("OPTIONAL");
    } else if (subjectParam) {
        await switchSubject(subjectParam);
    } else {
        updateSubjectHeader();
        await restoreOrCreateSession();
    }

    await loadSessionList();
}

function setupChatEventListeners() {
    messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendChatMessage();
        }
    });

    messageInput.addEventListener("input", () => {
        messageInput.style.height = "auto";
        messageInput.style.height = `${Math.min(messageInput.scrollHeight, 120)}px`;
    });
}

async function createNewSession() {
    try {
        const response = await fetch("/api/chat/new-session", {method: "POST"});
        const data = await response.json();
        sessionId = data.sessionId;
    } catch (error) {
        console.error("Failed to create session:", error);
    }
}

async function switchSubject(subject) {
    if (subject === "OPTIONAL") {
        isOptionalMode = true;
        currentSubject = "GENERAL";
    } else {
        isOptionalMode = false;
        currentSubject = subject || "GENERAL";
    }

    updateSubjectHeader();
    await restoreOrCreateSession();
}

function updateSubjectHeader() {
    const user = JSON.parse(localStorage.getItem("upscMentorUser"));
    const titleEl = document.getElementById("chatSubjectTitle");
    const badgeEl = document.getElementById("chatSubjectBadge");
    if (!titleEl || !badgeEl) return;

    if (isOptionalMode) {
        const optName = user?.optionalSubject
            ? user.optionalSubject.replace(/_/g, " ")
            : "Optional Subject";
        titleEl.textContent = `Optional: ${optName}`;
        badgeEl.textContent = "Optional Subject";
        return;
    }

    const subjectNames = {
        POLITY: "Polity & Governance",
        HISTORY_MODERN: "Modern History",
        HISTORY_ANCIENT: "Ancient History",
        HISTORY_MEDIEVAL: "Medieval History",
        HISTORY_ART_CULTURE: "Art & Culture",
        GEOGRAPHY_INDIAN: "Indian Geography",
        GEOGRAPHY_PHYSICAL: "Physical Geography",
        ECONOMY: "Indian Economy",
        ENVIRONMENT: "Environment & Ecology",
        SCIENCE_TECH: "Science & Technology",
        CURRENT_AFFAIRS: "Current Affairs",
        GS1: "GS Paper I",
        GS2: "GS Paper II",
        GS3: "GS Paper III",
        GS4: "Ethics (GS Paper IV)",
        ESSAY: "Essay",
        CSAT: "CSAT",
        GENERAL: "General Guidance"
    };

    const title = subjectNames[currentSubject] || "UPSC AI Mentor";
    titleEl.textContent = title;
    badgeEl.textContent = currentSubject.replace(/_/g, " ");
}

async function newChat() {
    await createNewSession();
    saveCurrentSession();
    chatMessages.innerHTML = getWelcomeHTML();
    await loadSessionList();
}

async function sendChatMessage() {
    const message = messageInput.value.trim();
    if (!message) return;

    if (!currentUser || !currentUser.id) {
        window.location.href = "/onboarding";
        return;
    }

    messageInput.value = "";
    messageInput.style.height = "auto";

    const welcome = chatMessages.querySelector(".welcome-message");
    if (welcome) welcome.remove();

    appendChatMessage("user", message);
    const typingId = showTypingIndicator();
    setInputEnabled(false);

    try {
        const parsedInput = parseMessageModelOverride(message);

        if (!sessionId) {
            await createNewSession();
        }

        const response = await fetch("/api/chat/send", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                message: parsedInput.message,
                subject: currentSubject,
                sessionId,
                userId: currentUser.id,
                optionalSubject: isOptionalMode,
                localModelName: parsedInput.localModelName
            })
        });

        const data = await response.json();
        removeTypingIndicator(typingId);

        if (data.success) {
            if (data.sessionId) {
                sessionId = data.sessionId;
                saveCurrentSession();
                await loadSessionList();
            }
            appendChatMessage("assistant", data.message);
        } else {
            appendChatMessage("assistant", `Error: ${data.error || "Something went wrong."}`);
        }
    } catch (error) {
        removeTypingIndicator(typingId);
        appendChatMessage("assistant", "Connection error. Please check if the server is running.");
    }

    setInputEnabled(true);
    messageInput.focus();
}

function parseMessageModelOverride(rawMessage) {
    const lines = rawMessage.split(/\r?\n/);
    if (lines.length === 0) {
        return { message: rawMessage, localModelName: null };
    }

    const firstLine = lines[0].trim();
    const match = firstLine.match(/^@model\s+([^\s]+)\s*$/i);
    if (!match) {
        return { message: rawMessage, localModelName: null };
    }

    const localModelName = match[1].trim();
    const message = lines.slice(1).join("\n").trim();

    return {
        message: message || rawMessage,
        localModelName
    };
}

function sendQuickMessage(message) {
    messageInput.value = message;
    sendChatMessage();
}

function getSessionStoreKey() {
    return `upscChatSessions_${currentUser?.id || "anonymous"}`;
}

function getCurrentModeKey() {
    return isOptionalMode ? "OPTIONAL" : currentSubject;
}

function readSessionMap() {
    const raw = localStorage.getItem(getSessionStoreKey());
    if (!raw) return {};
    try {
        return JSON.parse(raw);
    } catch (e) {
        return {};
    }
}

function writeSessionMap(map) {
    localStorage.setItem(getSessionStoreKey(), JSON.stringify(map));
}

function saveCurrentSession() {
    if (!sessionId) return;
    const map = readSessionMap();
    map[getCurrentModeKey()] = sessionId;
    writeSessionMap(map);
}

async function restoreOrCreateSession() {
    const map = readSessionMap();
    const existing = map[getCurrentModeKey()];
    if (existing) {
        sessionId = existing;
        await loadSessionHistory();
        return;
    }

    await createNewSession();
    saveCurrentSession();
    chatMessages.innerHTML = getWelcomeHTML();
}

async function loadSessionHistory() {
    if (!sessionId || !currentUser?.id) {
        chatMessages.innerHTML = getWelcomeHTML();
        return;
    }

    try {
        const response = await fetch(`/api/chat/session/${encodeURIComponent(sessionId)}?userId=${currentUser.id}`);
        if (!response.ok) {
            chatMessages.innerHTML = getWelcomeHTML();
            return;
        }
        const messages = await response.json();
        if (!Array.isArray(messages) || messages.length === 0) {
            chatMessages.innerHTML = getWelcomeHTML();
            return;
        }

        chatMessages.innerHTML = "";
        messages.forEach((msg) => {
            const role = msg.role === "USER" ? "user" : "assistant";
            appendChatMessage(role, msg.content);
        });
        scrollToBottom();
    } catch (error) {
        console.error("Failed to load session history:", error);
        chatMessages.innerHTML = getWelcomeHTML();
    }
}

async function loadSessionList() {
    if (!oldChatsSelect || !currentUser?.id) return;
    try {
        const response = await fetch(`/api/chat/sessions?userId=${currentUser.id}`);
        if (!response.ok) return;
        const sessions = await response.json();

        oldChatsSelect.innerHTML = '<option value="">Old chats</option>';
        sessions.forEach((s) => {
            const sid = s.sessionId;
            const preview = (s.lastMessage || "").replace(/\s+/g, " ").trim();
            const shortPreview = preview.length > 40 ? `${preview.slice(0, 40)}...` : preview;
            const option = document.createElement("option");
            option.value = sid;
            option.textContent = `${shortPreview || "Conversation"} (${s.messageCount})`;
            oldChatsSelect.appendChild(option);
        });

        if (sessionId) {
            oldChatsSelect.value = sessionId;
        }
    } catch (error) {
        console.error("Failed to load old chats:", error);
    }
}

async function loadSelectedOldChat() {
    if (!oldChatsSelect) return;
    const selected = oldChatsSelect.value;
    if (!selected) return;
    sessionId = selected;
    saveCurrentSession();
    await loadSessionHistory();
}

// ============================================
// ENHANCED MESSAGE RENDERING
// ============================================

// Icon mapping for AI response section headers
const SECTION_ICONS = {
    "concept": "💡",
    "deep dive": "📖",
    "upsc connection": "🎯",
    "common mistake": "⚠️",
    "try this": "🏋️",
    "summary": "📋",
    "key points": "🔑",
    "examples": "📌",
    "exam relevance": "📝",
    "prelims focus": "🔵",
    "mains focus": "🟠",
    "quick revision": "⚡",
    "conclusion": "🏁",
    "introduction": "📝",
    "background": "📜",
    "analysis": "🔍",
    "recommendations": "💡",
    "upsc tip": "💬",
    "important": "🔥",
    "note": "📝",
};

function getSectionIcon(heading) {
    const lower = heading.toLowerCase().trim();
    for (const [key, icon] of Object.entries(SECTION_ICONS)) {
        if (lower.includes(key)) return icon;
    }
    return "📘";
}

function enhanceAssistantResponse(html) {
    // Parse the rendered HTML
    const temp = document.createElement("div");
    temp.innerHTML = html;

    // Enhance headings with icons and section classes
    temp.querySelectorAll("h1, h2, h3, h4").forEach((heading) => {
        const text = heading.textContent.trim();
        const icon = getSectionIcon(text);

        heading.innerHTML = `<span class="section-icon">${icon}</span> ${text}`;
        heading.classList.add("response-heading");

        // Add section class based on content
        const lowerText = text.toLowerCase();
        if (lowerText.includes("upsc") || lowerText.includes("prelims") || lowerText.includes("mains")) {
            heading.parentElement?.classList.add("section-upsc");
        }
    });

    // Enhance lists with custom bullets
    temp.querySelectorAll("ul li").forEach((li) => {
        li.classList.add("custom-list-item");
    });

    temp.querySelectorAll("ol li").forEach((li) => {
        li.classList.add("ordered-list-item");
    });

    // Enhance tables
    temp.querySelectorAll("table").forEach((table) => {
        table.classList.add("response-table");
        const wrapper = document.createElement("div");
        wrapper.className = "table-wrapper";
        table.parentNode.insertBefore(wrapper, table);
        wrapper.appendChild(table);
    });

    // Enhance code blocks
    temp.querySelectorAll("pre code").forEach((code) => {
        const pre = code.parentElement;
        if (pre.tagName === "PRE") {
            pre.classList.add("code-block");
            const lang = code.className.replace("language-", "").replace("hljs ", "");
            if (lang) {
                pre.dataset.lang = lang;
            }
        }
    });

    // Enhance blockquotes
    temp.querySelectorAll("blockquote").forEach((bq) => {
        bq.classList.add("response-quote");
    });

    // Add response metadata
    const wordCount = temp.textContent.trim().split(/\s+/).length;
    const readTime = Math.max(1, Math.ceil(wordCount / 200));

    return {
        html: temp.innerHTML,
        wordCount,
        readTime
    };
}

function appendChatMessage(role, content) {
    const messageDiv = document.createElement("div");
    messageDiv.className = `message ${role}`;

    const avatar = role === "user" ? "U" : "🤖";

    let copyButtonHTML = "";
    let metadataHTML = "";
    let renderedContent;

    if (role === "assistant") {
        const enhanced = enhanceAssistantResponse(
            DOMPurify.sanitize(marked.parse(content), {ADD_ATTR: ['target']})
        );
        renderedContent = enhanced.html;

        copyButtonHTML = `<button class="message-copy-btn" title="Copy response" onclick="copyMessage(this)">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
        </button>`;

        metadataHTML = `<div class="message-meta">
            <span class="meta-item">📝 ${enhanced.wordCount} words</span>
            <span class="meta-item">⏱️ ${enhanced.readTime} min read</span>
            <span class="meta-item">${formatTimestamp()}</span>
        </div>`;
    } else {
        renderedContent = escapeHtml(content);
    }

    messageDiv.innerHTML = `
        <div class="message-avatar">${avatar}</div>
        <div class="message-content">
            ${copyButtonHTML}
            <div class="message-text">${renderedContent}</div>
            ${metadataHTML}
        </div>
    `;

    chatMessages.appendChild(messageDiv);

    // Apply syntax highlighting to code blocks
    messageDiv.querySelectorAll("pre code").forEach((block) => {
        if (window.hljs) {
            window.hljs.highlightElement(block);
        }
    });

    scrollToBottom();
}

function formatTimestamp() {
    const now = new Date();
    return now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
}

function showTypingIndicator() {
    const id = `typing-${Date.now()}`;
    const typingDiv = document.createElement("div");
    typingDiv.id = id;
    typingDiv.className = "message assistant";
    typingDiv.innerHTML = `
        <div class="message-avatar">🤖</div>
        <div class="message-content">
            <div class="typing-indicator">
                <span></span><span></span><span></span>
            </div>
            <span class="typing-text">UPSC Guru is thinking...</span>
        </div>
    `;
    chatMessages.appendChild(typingDiv);
    scrollToBottom();
    return id;
}

function removeTypingIndicator(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
}

function setInputEnabled(enabled) {
    messageInput.disabled = !enabled;
    sendBtn.disabled = !enabled;
}

function scrollToBottom() {
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

async function copyMessage(btn) {
    const messageContent = btn.closest(".message").querySelector(".message-text");
    const text = messageContent.innerText;

    try {
        await navigator.clipboard.writeText(text);
        btn.innerHTML = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>`;
        btn.classList.add("copied");
        setTimeout(() => {
            btn.innerHTML = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>`;
            btn.classList.remove("copied");
        }, 2000);
    } catch (err) {
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

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
}

function getWelcomeHTML() {
    const user = JSON.parse(localStorage.getItem("upscMentorUser"));
    const optionalName = user?.optionalSubject
        ? user.optionalSubject.replace(/_/g, " ")
        : "your optional subject";

    return `
        <div class="welcome-message">
            <div class="welcome-icon">🎓</div>
            <h2>Welcome to UPSC AI Tutor</h2>
            <p>Ask questions on any UPSC topic and get exam-focused guidance.</p>
            <div class="quick-actions">
                <button class="quick-btn" onclick="sendQuickMessage('Explain Fundamental Rights under Indian Constitution')">⚖️ Fundamental Rights</button>
                <button class="quick-btn" onclick="sendQuickMessage('What are important economy topics for UPSC prelims?')">💰 Economy Topics</button>
                <button class="quick-btn" onclick="sendQuickMessage('Explain federalism in India with examples')">🏛️ Federalism</button>
                <button class="quick-btn" onclick="sendQuickMessage('Give me a strategy for ${optionalName}')">📚 Optional Strategy</button>
            </div>
        </div>
    `;
}

window.copyMessage = copyMessage;
