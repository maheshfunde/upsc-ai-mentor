const API_BASE = '/api';

function getUserFromStorage() {
    const userData = localStorage.getItem('upscMentorUser');
    return userData ? JSON.parse(userData) : null;
}

async function loadLlmConfigStatus(userId) {
    const statusEl = document.getElementById('llmStatusText');
    const onlineModelInput = document.getElementById('onlineModelInput');
    const baseUrlInput = document.getElementById('baseUrlInput');
    const localModelInput = document.getElementById('localModelInput');
    if (!statusEl) return;

    try {
        const response = await fetch(`${API_BASE}/user/${userId}/llm-config`);
        if (!response.ok) return;

        const data = await response.json();
        if (data.onlineConfigured) {
            const modelName = data.modelName || 'gpt-4o-mini';
            statusEl.textContent = `Current mode: Online (${modelName})`;
            if (onlineModelInput) onlineModelInput.value = data.modelName || '';
            if (baseUrlInput) baseUrlInput.value = data.baseUrl || '';
        } else {
            const localModelName = data.localModelName || 'default';
            statusEl.textContent = `Current mode: Local Ollama (${localModelName})`;
        }
        if (localModelInput) localModelInput.value = data.localModelName || '';
    } catch (error) {
        console.log('Could not load LLM config status', error);
    }
}

async function saveOnlineLlmConfig() {
    const user = getUserFromStorage();
    const apiKeyInput = document.getElementById('apiKeyInput');
    const baseUrlInput = document.getElementById('baseUrlInput');
    const onlineModelInput = document.getElementById('onlineModelInput');
    const hint = document.getElementById('llmConfigHint');
    if (!user || !apiKeyInput) return;

    const apiKey = apiKeyInput.value.trim();
    const baseUrl = baseUrlInput ? baseUrlInput.value.trim() : '';
    const modelName = onlineModelInput ? onlineModelInput.value.trim() : '';

    if (!apiKey) {
        if (hint) {
            hint.textContent = 'Enter an API key.';
            hint.className = 'field-hint error';
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/user/${user.id}/llm-config`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ apiKey, modelName, baseUrl })
        });
        const data = await response.json();
        if (response.ok && data.success) {
            if (hint) {
                hint.textContent = 'Online model enabled.';
                hint.className = 'field-hint success';
            }
            apiKeyInput.value = '';
            if (baseUrlInput) baseUrlInput.value = '';
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

async function useLocalLlm() {
    const user = getUserFromStorage();
    const localModelInput = document.getElementById('localModelInput');
    const hint = document.getElementById('llmConfigHint');
    if (!user || !localModelInput) return;

    const localModelName = localModelInput.value.trim();
    if (!localModelName) {
        if (hint) {
            hint.textContent = 'Enter a local model name.';
            hint.className = 'field-hint error';
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/user/${user.id}/llm-config/local`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ localModelName })
        });
        const data = await response.json();
        if (response.ok && data.success) {
            const apiKeyInput = document.getElementById('apiKeyInput');
            const onlineModelInput = document.getElementById('onlineModelInput');
            const baseUrlInput = document.getElementById('baseUrlInput');
            if (hint) {
                hint.textContent = `Switched to local model ${localModelName}.`;
                hint.className = 'field-hint success';
            }
            if (apiKeyInput) apiKeyInput.value = '';
            if (onlineModelInput) onlineModelInput.value = '';
            if (baseUrlInput) baseUrlInput.value = '';
            await loadLlmConfigStatus(user.id);
        } else if (hint) {
            hint.textContent = data.error || 'Failed to switch to local model.';
            hint.className = 'field-hint error';
        }
    } catch (error) {
        if (hint) {
            hint.textContent = 'Network error while saving local model.';
            hint.className = 'field-hint error';
        }
    }
}

async function clearLlmConfig() {
    const user = getUserFromStorage();
    const hint = document.getElementById('llmConfigHint');
    const apiKeyInput = document.getElementById('apiKeyInput');
    const baseUrlInput = document.getElementById('baseUrlInput');
    const onlineModelInput = document.getElementById('onlineModelInput');
    if (!user) return;

    try {
        const response = await fetch(`${API_BASE}/user/${user.id}/llm-config`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (response.ok && data.success) {
            if (hint) {
                hint.textContent = 'Online settings disabled. Using saved local model.';
                hint.className = 'field-hint success';
            }
            if (apiKeyInput) apiKeyInput.value = '';
            if (onlineModelInput) onlineModelInput.value = '';
            if (baseUrlInput) baseUrlInput.value = '';
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

window.saveOnlineLlmConfig = saveOnlineLlmConfig;
window.useLocalLlm = useLocalLlm;
window.clearLlmConfig = clearLlmConfig;
