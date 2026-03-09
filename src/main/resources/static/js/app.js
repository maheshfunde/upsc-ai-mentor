// ============================================
// UPSC AI MENTOR - GLOBAL APP LOGIC
// ============================================

const API_BASE = '/api';
let currentUser = null;

// ============================================
// USER MANAGEMENT
// ============================================

function getUserFromStorage() {
    const userData = localStorage.getItem('upscMentorUser');
    if (userData) {
        currentUser = JSON.parse(userData);
        return currentUser;
    }
    return null;
}

function saveUserToStorage(user) {
    localStorage.setItem('upscMentorUser', JSON.stringify(user));
    currentUser = user;
}

function formatEnumLabel(value) {
    if (!value) return '';
    return value
        .toString()
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, c => c.toUpperCase());
}

function checkAuth() {
    const user = getUserFromStorage();
    if (!user && !window.location.pathname.includes('onboarding')
        && window.location.pathname !== '/') {
        window.location.href = '/onboarding';
        return false;
    }
    return true;
}

// ============================================
// ONBOARDING
// ============================================

let currentStep = 1;
let selectedOptionalSubject = null;

function nextStep(step) {
    // Validate current step
    if (step === 2 && !validateStep1()) return;
    if (step === 3 && !selectedOptionalSubject) {
        alert('Please select an optional subject');
        return;
    }

    document.querySelectorAll('.form-step').forEach(s => s.classList.remove('active'));
    document.getElementById(`step${step}`).classList.add('active');

    document.querySelectorAll('.steps-indicator .step').forEach((s, i) => {
        s.classList.toggle('active', i < step);
        s.classList.toggle('completed', i < step - 1);
    });

    currentStep = step;
}

function prevStep(step) {
    document.querySelectorAll('.form-step').forEach(s => s.classList.remove('active'));
    document.getElementById(`step${step}`).classList.add('active');
    currentStep = step;
}

function validateStep1() {
    const name = document.getElementById('name').value.trim();
    const username = document.getElementById('username').value.trim();

    if (!name) { alert('Please enter your name'); return false; }
    if (!username || username.length < 3) {
        alert('Please enter a username (min 3 characters)');
        return false;
    }
    return true;
}

function selectOptional(card) {
    // Remove previous selection
    document.querySelectorAll('.optional-card').forEach(c => c.classList.remove('selected'));

    // Select this card
    card.classList.add('selected');
    selectedOptionalSubject = card.dataset.value;
    document.getElementById('selectedOptional').value = selectedOptionalSubject;

    // Enable next button
    document.getElementById('step2Next').disabled = false;
}

function filterOptionalSubjects() {
    const search = document.getElementById('optionalSearch').value.toLowerCase();
    document.querySelectorAll('.optional-card').forEach(card => {
        const name = card.querySelector('.optional-name').textContent.toLowerCase();
        card.style.display = name.includes(search) ? 'flex' : 'none';
    });
}

function showCategory(category) {
    document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');

    document.querySelectorAll('.optional-card').forEach(card => {
        if (category === 'all') {
            card.style.display = 'flex';
        } else {
            const categories = card.dataset.category || '';
            card.style.display = categories.includes(category) ? 'flex' : 'none';
        }
    });
}

function updateHoursLabel(value) {
    document.getElementById('hoursLabel').textContent = value + ' hours/day';
}

async function loginWithExistingUsername() {
    const input = document.getElementById('existingUsername');
    const hint = document.getElementById('existingUserHint');
    if (!input) return;

    const username = input.value.trim();
    if (!username) {
        if (hint) {
            hint.textContent = 'Enter username to continue.';
            hint.className = 'field-hint error';
        }
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/user/by-username/${encodeURIComponent(username)}`);
        if (!response.ok) {
            if (hint) {
                hint.textContent = 'Username not found.';
                hint.className = 'field-hint error';
            }
            return;
        }

        const user = await response.json();
        saveUserToStorage(user);
        if (hint) {
            hint.textContent = 'Found profile. Redirecting...';
            hint.className = 'field-hint success';
        }
        window.location.href = '/dashboard';
    } catch (error) {
        if (hint) {
            hint.textContent = 'Network error. Please try again.';
            hint.className = 'field-hint error';
        }
    }
}

async function submitProfile() {
    const weakSubjects = Array.from(document.querySelectorAll('input[name="weakSubject"]:checked'))
        .map(cb => cb.value).join(', ');

    const strongSubjects = Array.from(document.querySelectorAll('input[name="strongSubject"]:checked'))
        .map(cb => cb.value).join(', ');

    const profileData = {
        name: document.getElementById('name').value.trim(),
        username: document.getElementById('username').value.trim(),
        optionalSubject: selectedOptionalSubject,
        difficultyLevel: document.getElementById('difficultyLevel').value,
        targetYear: parseInt(document.getElementById('targetYear').value),
        attemptNumber: parseInt(document.getElementById('attemptNumber').value),
        dailyStudyHours: parseInt(document.getElementById('dailyHours').value),
        weakSubjects: weakSubjects || 'Not specified',
        strongSubjects: strongSubjects || 'Not specified'
    };

    // Show loading
    document.querySelectorAll('.form-step').forEach(s => s.classList.remove('active'));
    document.getElementById('loadingStep').style.display = 'block';

    try {
        const response = await fetch(`${API_BASE}/user/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(profileData)
        });

        const data = await response.json();

        if (data.success) {
            // Save user data locally
            saveUserToStorage({
                id: data.userId,
                ...profileData
            });

            // Redirect to dashboard
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1500);
        } else {
            alert('Error: ' + (data.error || 'Registration failed'));
            document.getElementById('loadingStep').style.display = 'none';
            document.getElementById('step3').classList.add('active');
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Network error. Please make sure the server is running.');
        document.getElementById('loadingStep').style.display = 'none';
        document.getElementById('step3').classList.add('active');
    }
}

// ============================================
// DASHBOARD
// ============================================

async function loadDashboard() {
    const user = getUserFromStorage();
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }

    // Update sidebar user info
    const nameEl = document.getElementById('sidebarUserName');
    const optionalEl = document.getElementById('sidebarOptional');
    if (nameEl) nameEl.textContent = user.name;
    if (optionalEl) optionalEl.textContent = user.optionalSubject
        ? formatEnumLabel(user.optionalSubject)
        : '';

    // Update welcome message
    const welcomeMsg = document.getElementById('welcomeMsg');
    if (welcomeMsg) welcomeMsg.textContent = `Welcome back, ${user.name}! 🎯`;

    const targetStat = document.getElementById('targetYearStat');
    if (targetStat) targetStat.textContent = user.targetYear || '2025';

    const levelStat = document.getElementById('levelStat');
    if (levelStat) levelStat.textContent = user.difficultyLevel
        ? formatEnumLabel(user.difficultyLevel)
        : '-';

    const optionalStat = document.getElementById('optionalSubjectStat');
    if (optionalStat) optionalStat.textContent = user.optionalSubject
        ? formatEnumLabel(user.optionalSubject)
        : '-';

    const optionalAction = document.getElementById('optionalAction');
    if (optionalAction) optionalAction.textContent = 'Study ' +
        (user.optionalSubject ? formatEnumLabel(user.optionalSubject) : 'Optional');

    // Load progress data
    try {
        const response = await fetch(`${API_BASE}/study-plan/progress/${user.id}`);
        if (response.ok) {
            const progressData = await response.json();
            updateDashboardStats(progressData);
        }
    } catch (error) {
        console.log('Could not load progress data:', error);
    }
}

function updateDashboardStats(data) {
    const totalSessions = document.getElementById('totalSessions');
    if (totalSessions) totalSessions.textContent = data.totalChatSessions || 0;

    const totalQuizzes = document.getElementById('totalQuizzes');
    if (totalQuizzes) totalQuizzes.textContent = data.totalQuizzes || 0;

    const avgScore = document.getElementById('avgScore');
    if (avgScore) {
        const score = data.averageScore ? Math.round(data.averageScore) : 0;
        avgScore.textContent = score + '%';
    }

    // Subject-wise progress bars
    const progressGrid = document.getElementById('subjectProgress');
    if (progressGrid && data.subjectScores) {
        const scores = data.subjectScores;
        if (Object.keys(scores).length > 0) {
            progressGrid.innerHTML = Object.entries(scores).map(([subject, score]) => `
                <div class="progress-item">
                    <div class="progress-info">
                        <span class="progress-subject">${subject}</span>
                        <span class="progress-score">${Math.round(score)}%</span>
                    </div>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" 
                             style="width: ${score}%; 
                             background: ${score >= 70 ? '#22c55e' : score >= 50 ? '#f59e0b' : '#ef4444'}">
                        </div>
                    </div>
                </div>
            `).join('');
        }
    }
}

async function getAiAnalysis() {
    const user = getUserFromStorage();
    if (!user) return;

    const analysisDiv = document.getElementById('aiAnalysis');
    analysisDiv.innerHTML = '<div class="loading-spinner-small"></div><p>Generating AI analysis...</p>';

    try {
        const response = await fetch(`${API_BASE}/study-plan/analysis/${user.id}`);
        const data = await response.json();

        if (data.success) {
            analysisDiv.innerHTML = marked.parse(data.analysis);
        } else {
            analysisDiv.innerHTML = '<p>Could not generate analysis. Try again later.</p>';
        }
    } catch (error) {
        analysisDiv.innerHTML = '<p>Error connecting to server. Make sure Ollama is running.</p>';
    }
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

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // Check authentication for protected pages
    const publicPages = ['/', '/onboarding'];
    if (!publicPages.includes(window.location.pathname)) {
        checkAuth();
    }
});
