// ============================================
// UPSC AI MENTOR - ANSWER PRACTICE LOGIC
// ============================================

let currentQuestion = '';
let timerInterval = null;
let timerSeconds = 0;
let timerDuration = 0;
let timerRunning = false;

document.addEventListener('DOMContentLoaded', () => {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }

    const userAnswer = document.getElementById('userAnswer');
    if (userAnswer) {
        userAnswer.addEventListener('input', updateWordCount);
        userAnswer.addEventListener('input', autoExpandTextarea);
    }

    const wordLimitSelect = document.getElementById('wordLimit');
    if (wordLimitSelect) {
        wordLimitSelect.addEventListener('change', () => {
            const el = document.getElementById('wordCounter');
            if (el) el.textContent = `0 / ${wordLimitSelect.value} words`;
        });
    }
});

// ============================================
// WORD COUNT & AUTO-EXPAND
// ============================================

function updateWordCount() {
    const text = document.getElementById('userAnswer').value.trim();
    const wordCount = text ? text.split(/\s+/).length : 0;
    const targetWords = parseInt(document.getElementById('wordLimit').value);
    const countEl = document.getElementById('wordCounter');

    if (countEl) {
        countEl.textContent = `${wordCount} / ${targetWords} words`;
        countEl.className = 'input-counter';

        if (wordCount > targetWords * 1.1) {
            countEl.classList.add('danger');
        } else if (wordCount >= targetWords * 0.9) {
            countEl.classList.add('warning');
        }
    }
}

function autoExpandTextarea() {
    const el = this;
    el.style.height = 'auto';
    el.style.height = Math.max(300, Math.min(el.scrollHeight, 600)) + 'px';
}

// ============================================
// ANSWER WRITING TIMER
// ============================================

function setTimer(minutes) {
    timerDuration = minutes * 60;
    timerSeconds = timerDuration;
    timerRunning = false;
    clearInterval(timerInterval);

    document.getElementById('timerDisplay').textContent = formatTime(timerSeconds);
    document.getElementById('timerDisplay').className = 'timer-display';

    // Update preset buttons
    document.querySelectorAll('.timer-preset').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    if (minutes === 0) {
        document.getElementById('timerContainer').style.display = 'none';
    } else {
        document.getElementById('timerContainer').style.display = 'flex';
        document.getElementById('timerStartBtn').style.display = '';
        document.getElementById('timerPauseBtn').style.display = 'none';
    }
}

function startTimer() {
    if (timerRunning) return;
    timerRunning = true;

    document.getElementById('timerStartBtn').style.display = 'none';
    document.getElementById('timerPauseBtn').style.display = '';

    timerInterval = setInterval(() => {
        timerSeconds--;
        document.getElementById('timerDisplay').textContent = formatTime(timerSeconds);

        if (timerSeconds <= 30 && timerSeconds > 0) {
            document.getElementById('timerDisplay').className = 'timer-display warning';
        } else if (timerSeconds <= 0) {
            document.getElementById('timerDisplay').className = 'timer-display danger';
            clearInterval(timerInterval);
            timerRunning = false;
            alert('⏰ Time is up! Please submit your answer now.');
        }
    }, 1000);
}

function pauseTimer() {
    clearInterval(timerInterval);
    timerRunning = false;

    document.getElementById('timerStartBtn').style.display = '';
    document.getElementById('timerPauseBtn').style.display = 'none';
}

function resetTimer() {
    clearInterval(timerInterval);
    timerRunning = false;
    timerSeconds = timerDuration;

    document.getElementById('timerDisplay').textContent = formatTime(timerSeconds);
    document.getElementById('timerDisplay').className = 'timer-display';
    document.getElementById('timerStartBtn').style.display = '';
    document.getElementById('timerPauseBtn').style.display = 'none';
}

function formatTime(seconds) {
    if (seconds <= 0) return '00:00';
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

// ============================================
// ANSWER TEMPLATES
// ============================================

function insertTemplate(type) {
    const textarea = document.getElementById('userAnswer');
    const pos = textarea.selectionStart;
    const before = textarea.value.substring(0, pos);
    const after = textarea.value.substring(pos);

    let template = '';
    switch (type) {
        case 'intro':
            template = '\n**Introduction:**\n[Start with a brief introduction that directly addresses the question. Define key terms and set the context.]\n\n';
            break;
        case 'body':
            template = '\n**Key Points:**\n1. \n2. \n3. \n\n';
            break;
        case 'conclusion':
            template = '\n**Conclusion:**\n[Summarize the key arguments and provide a forward-looking perspective. Suggest reforms or improvements.]\n\n';
            break;
        case 'points':
            template = '\n• \n• \n• \n\n';
            break;
    }

    textarea.value = before + template + after;
    textarea.focus();
    updateWordCount();
}

// ============================================
// QUESTION GENERATION
// ============================================

async function generateQuestion() {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    const subject = document.getElementById('answerSubject').value;
    const isOptional = document.getElementById('isOptionalAnswer').checked;

    document.getElementById('questionSection').style.display = 'none';
    document.getElementById('answerSection').style.display = 'none';
    document.getElementById('evaluationSection').style.display = 'none';
    document.getElementById('loadingSection').style.display = 'block';
    document.getElementById('loadingText').textContent = 'Generating practice question...';

    try {
        const response = await fetch(
            `/api/answer/generate-question?userId=${user.id}&subject=${subject}&isOptional=${isOptional}`
        );

        const data = await response.json();

        document.getElementById('loadingSection').style.display = 'none';

        if (data.success === 'true' || data.question) {
            currentQuestion = data.question;
            document.getElementById('questionDisplay').innerHTML = marked.parse(data.question);
            document.getElementById('questionSection').style.display = 'block';
            document.getElementById('answerSection').style.display = 'block';

            document.getElementById('userAnswer').value = '';
            updateWordCount();

            // Reset timer
            clearInterval(timerInterval);
            timerRunning = false;
            document.getElementById('timerContainer').style.display = 'none';

            document.getElementById('questionSection').scrollIntoView({ behavior: 'smooth' });
        } else {
            alert('Could not generate question. Please try again.');
        }
    } catch (error) {
        document.getElementById('loadingSection').style.display = 'none';
        alert('Connection error. Make sure the server and Ollama are running.');
        console.error('Question generation error:', error);
    }
}

async function generateRandomQuestion() {
    const subjects = ['POLITY', 'ECONOMY', 'HISTORY_MODERN', 'ENVIRONMENT', 'SCIENCE_TECH', 'GS4'];
    const randomSubject = subjects[Math.floor(Math.random() * subjects.length)];

    document.getElementById('answerSubject').value = randomSubject;
    generateQuestion();
}

// ============================================
// ANSWER SUBMISSION & EVALUATION
// ============================================

async function submitAnswer() {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    const userAnswer = document.getElementById('userAnswer').value.trim();
    const subject = document.getElementById('answerSubject').value;
    const wordLimit = parseInt(document.getElementById('wordLimit').value);
    const isOptional = document.getElementById('isOptionalAnswer').checked;

    if (!userAnswer) {
        alert('Please write your answer before submitting.');
        return;
    }

    if (userAnswer.split(/\s+/).length < 20) {
        alert('Your answer is too short. Please write at least a few sentences.');
        return;
    }

    // Stop timer if running
    if (timerRunning) {
        clearInterval(timerInterval);
        timerRunning = false;
    }

    document.getElementById('answerSection').style.display = 'none';
    document.getElementById('loadingSection').style.display = 'block';
    document.getElementById('loadingText').textContent = 'AI is evaluating your answer... This may take a moment.';

    try {
        const response = await fetch('/api/answer/evaluate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: user.id,
                subject: subject,
                question: currentQuestion,
                userAnswer: userAnswer,
                wordLimit: wordLimit,
                optionalSubject: isOptional
            })
        });

        const data = await response.json();

        document.getElementById('loadingSection').style.display = 'none';

        if (data.success) {
            displayEvaluation(data);
        } else {
            alert('Could not evaluate answer: ' + (data.error || 'Unknown error'));
            document.getElementById('answerSection').style.display = 'block';
        }
    } catch (error) {
        document.getElementById('loadingSection').style.display = 'none';
        document.getElementById('answerSection').style.display = 'block';
        alert('Connection error. Please try again.');
        console.error('Evaluation error:', error);
    }
}

function displayEvaluation(data) {
    document.getElementById('overallScore').textContent = data.overallScore || '-';
    document.getElementById('contentScore').textContent = data.contentScore || '-';
    document.getElementById('structureScore').textContent = data.structureScore || '-';
    document.getElementById('analyticalScore').textContent = data.analyticalScore || '-';

    colorScore('overallScore', data.overallScore);
    colorScore('contentScore', data.contentScore);
    colorScore('structureScore', data.structureScore);
    colorScore('analyticalScore', data.analyticalScore);

    document.getElementById('strengthsFeedback').innerHTML = marked.parse(data.strengths || 'Not available');
    document.getElementById('weaknessesFeedback').innerHTML = marked.parse(data.weaknesses || 'Not available');
    document.getElementById('suggestionsFeedback').innerHTML = marked.parse(data.suggestions || 'Not available');
    document.getElementById('missedFeedback').innerHTML = marked.parse(data.dimensionsMissed || 'None identified');

    document.getElementById('modelAnswer').innerHTML = marked.parse(data.modelAnswer || 'Model answer not available.');

    document.getElementById('evaluationSection').style.display = 'block';
    document.getElementById('evaluationSection').scrollIntoView({ behavior: 'smooth' });
}

function colorScore(elementId, score) {
    const el = document.getElementById(elementId);
    if (!el || !score) return;

    if (score >= 8) el.style.color = '#22c55e';
    else if (score >= 6) el.style.color = '#f59e0b';
    else if (score >= 4) el.style.color = '#f97316';
    else el.style.color = '#ef4444';
}

function resetPractice() {
    currentQuestion = '';
    document.getElementById('questionSection').style.display = 'none';
    document.getElementById('answerSection').style.display = 'none';
    document.getElementById('evaluationSection').style.display = 'none';
    document.getElementById('userAnswer').value = '';
    clearInterval(timerInterval);
    timerRunning = false;
    updateWordCount();

    window.scrollTo({ top: 0, behavior: 'smooth' });
}
