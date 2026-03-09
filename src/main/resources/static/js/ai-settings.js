const API_BASE = '/api';

function getUserFromStorage() {
    const userData = localStorage.getItem('upscMentorUser');
    return userData ? JSON.parse(userData) : null;
}

async function loadLlmConfigStatus(userId) {
    const statusEl = document.getElementById('llmStatusText');
    const modelInput = document.getElementById('onlineModelInput');
    if (!statusEl) return;

    try {
        const response = await fetch(`${API_BASE}/user/${userId}/llm-config`);
        if (!response.ok) return;

        const data = await response.json();
        if (data.onlineConfigured) {
            const modelName = data.modelName || 'gpt-4o-mini';
            statusEl.textContent = `Current mode: Online (${modelName})`;
            if (modelInput) modelInput.value = modelName;
        } else {
            statusEl.textContent = 'Current mode: Local Ollama model';
            if (modelInput) modelInput.value = data.modelName || '';
        }
    } catch (error) {
        console.log('Could not load LLM config status', error);
    }
}

async function saveLlmConfig() {
    const user = getUserFromStorage();
    const apiKeyInput = document.getElementById('apiKeyInput');
    const modelInput = document.getElementById('onlineModelInput');
    const hint = document.getElementById('llmConfigHint');
    if (!user || !apiKeyInput) return;

    const apiKey = apiKeyInput.value.trim();
    const modelName = modelInput ? modelInput.value.trim() : '';

    if (!apiKey) {
        if (hint) {
            hint.textContent = 'API key is required.';
            hint.className = 'field-hint error';
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/user/${user.id}/llm-config`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ apiKey, modelName })
        });
        const data = await response.json();
        if (response.ok && data.success) {
            if (hint) {
                hint.textContent = 'Online model enabled.';
                hint.className = 'field-hint success';
            }
            apiKeyInput.value = '';
            await loadLlmConfigStatus(user.id);
        } else if (hint) {
            hint.textContent = data.error || 'Failed to save API key.';
            hint.className = 'field-hint error';
        }
    } catch (error) {
        if (hint) {
            hint.textContent = 'Network error while saving config.';
            hint.className = 'field-hint error';
        }
    }
}

async function clearLlmConfig() {
    const user = getUserFromStorage();
    const hint = document.getElementById('llmConfigHint');
    const apiKeyInput = document.getElementById('apiKeyInput');
    if (!user) return;

    try {
        const response = await fetch(`${API_BASE}/user/${user.id}/llm-config`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (response.ok && data.success) {
            if (hint) {
                hint.textContent = 'Switched to local LLM.';
                hint.className = 'field-hint success';
            }
            if (apiKeyInput) apiKeyInput.value = '';
            await loadLlmConfigStatus(user.id);
        } else if (hint) {
            hint.textContent = data.error || 'Failed to clear config.';
            hint.className = 'field-hint error';
        }
    } catch (error) {
        if (hint) {
            hint.textContent = 'Network error while clearing config.';
            hint.className = 'field-hint error';
        }
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    const user = getUserFromStorage();
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }
    await loadLlmConfigStatus(user.id);
});
