// ============================================
// UPSC AI MENTOR - ANSWER PRACTICE LOGIC
// ============================================

let currentQuestion = '';

document.addEventListener('DOMContentLoaded', () => {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }

    // Word counter for answer textarea
    const userAnswer = document.getElementById('userAnswer');
    if (userAnswer) {
        userAnswer.addEventListener('input', updateWordCount);
    }

    // Update target words when word limit changes
    const wordLimitSelect = document.getElementById('wordLimit');
    if (wordLimitSelect) {
        wordLimitSelect.addEventListener('change', () => {
            document.getElementById('targetWords').textContent = wordLimitSelect.value;
        });
    }
});

function updateWordCount() {
    const text = document.getElementById('userAnswer').value.trim();
    const wordCount = text ? text.split(/\s+/).length : 0;
    const countEl = document.getElementById('wordCount');
    const targetWords = parseInt(document.getElementById('targetWords').textContent);

    countEl.textContent = wordCount;

    // Color coding
    if (wordCount > targetWords * 1.1) {
        countEl.style.color = '#ef4444'; // Red - over limit
    } else if (wordCount >= targetWords * 0.8) {
        countEl.style.color = '#22c55e'; // Green - good range
    } else {
        countEl.style.color = '#f59e0b'; // Yellow - need more
    }
}

async function generateQuestion() {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    const subject = document.getElementById('answerSubject').value;
    const isOptional = document.getElementById('isOptionalAnswer').checked;

    // Show loading
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

            // Clear previous answer
            document.getElementById('userAnswer').value = '';
            updateWordCount();

            // Scroll to question
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

    // Show loading
    document.getElementById('answerSection').style.display = 'none';
    document.getElementById('loadingSection').style.display = 'block';
    document.getElementById('loadingText').textContent =
        'AI is evaluating your answer... This may take a moment.';

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
    // Scores
    document.getElementById('overallScore').textContent = data.overallScore || '-';
    document.getElementById('contentScore').textContent = data.contentScore || '-';
    document.getElementById('structureScore').textContent = data.structureScore || '-';
    document.getElementById('analyticalScore').textContent = data.analyticalScore || '-';

    // Color code scores
    colorScore('overallScore', data.overallScore);
    colorScore('contentScore', data.contentScore);
    colorScore('structureScore', data.structureScore);
    colorScore('analyticalScore', data.analyticalScore);

    // Feedback
    document.getElementById('strengthsFeedback').innerHTML =
        marked.parse(data.strengths || 'Not available');
    document.getElementById('weaknessesFeedback').innerHTML =
        marked.parse(data.weaknesses || 'Not available');
    document.getElementById('suggestionsFeedback').innerHTML =
        marked.parse(data.suggestions || 'Not available');
    document.getElementById('missedFeedback').innerHTML =
        marked.parse(data.dimensionsMissed || 'None identified');

    // Model answer
    document.getElementById('modelAnswer').innerHTML =
        marked.parse(data.modelAnswer || 'Model answer not available.');

    // Show evaluation section
    document.getElementById('evaluationSection').style.display = 'block';
    document.getElementById('evaluationSection').scrollIntoView({ behavior: 'smooth' });
}

function colorScore(elementId, score) {
    const el = document.getElementById(elementId);
    if (!el || !score) return;

    if (score >= 8) el.style.color = '#22c55e';       // Green
    else if (score >= 6) el.style.color = '#f59e0b';   // Yellow
    else if (score >= 4) el.style.color = '#f97316';    // Orange
    else el.style.color = '#ef4444';                     // Red
}

function resetPractice() {
    currentQuestion = '';
    document.getElementById('questionSection').style.display = 'none';
    document.getElementById('answerSection').style.display = 'none';
    document.getElementById('evaluationSection').style.display = 'none';
    document.getElementById('userAnswer').value = '';
    updateWordCount();

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}