let quizQuestions = [];
let currentQuestionIndex = 0;
let userAnswers = {};

document.addEventListener('DOMContentLoaded', () => {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));
    if (!user) {
        window.location.href = '/onboarding';
        return;
    }
});

async function startQuiz() {
    const user = JSON.parse(localStorage.getItem('upscMentorUser'));

    const subject = document.getElementById('quizSubject').value;
    const topic = document.getElementById('quizTopic').value;
    const count = parseInt(document.getElementById('quizCount').value);
    const difficulty = document.getElementById('quizDifficulty').value;
    const isOptional = document.getElementById('isOptionalQuiz').checked;

    // Show loading
    document.getElementById('quizSetup').style.display = 'none';
    document.getElementById('quizLoading').style.display = 'block';

    try {
        const response = await fetch('/api/quiz/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: user.id,
                subject: subject,
                specificTopic: topic || null,
                numberOfQuestions: count,
                difficulty: difficulty,
                optionalSubject: isOptional
            })
        });

        const data = await response.json();

        if (data.success) {
            // Parse quiz questions from AI response
            quizQuestions = parseQuizQuestions(data.quiz);

            if (quizQuestions.length > 0) {
                currentQuestionIndex = 0;
                userAnswers = {};

                document.getElementById('quizLoading').style.display = 'none';
                document.getElementById('quizArea').style.display = 'block';

                renderQuestion();
            } else {
                alert('Could not parse quiz questions. Please try again.');
                resetQuiz();
            }
        } else {
            alert('Error generating quiz. Please try again.');
            resetQuiz();
        }
    } catch (error) {
        console.error('Quiz generation error:', error);
        alert('Connection error. Make sure the server is running.');
        resetQuiz();
    }
}

function parseQuizQuestions(quizData) {
    try {
        // Try to extract JSON array from the response
        let jsonStr = quizData;

        // Find JSON array in the response
        const arrayStart = jsonStr.indexOf('[');
        const arrayEnd = jsonStr.lastIndexOf(']');

        if (arrayStart !== -1 && arrayEnd > arrayStart) {
            jsonStr = jsonStr.substring(arrayStart, arrayEnd + 1);
        }

        // Clean up common issues
        jsonStr = jsonStr.replace(/```json/g, '').replace(/```/g, '').trim();

        const questions = JSON.parse(jsonStr);

        if (Array.isArray(questions) && questions.length > 0) {
            return questions.map((q, index) => ({
                id: index,
                question: q.question || `Question ${index + 1}`,
                options: q.options || {},
                correctAnswer: q.correctAnswer || 'A',
                explanation: q.explanation || 'No explanation provided.',
                upscRelevance: q.upscRelevance || ''
            }));
        }
    } catch (e) {
        console.error('Failed to parse quiz JSON:', e);

        // Fallback: Try to extract questions from text
        return extractQuestionsFromText(quizData);
    }

    return [];
}

function extractQuestionsFromText(text) {
    // Basic text parsing fallback
    const questions = [];
    const questionBlocks = text.split(/\d+\.\s/);

    questionBlocks.forEach((block, index) => {
        if (block.trim().length > 20) {
            const lines = block.trim().split('\n').filter(l => l.trim());
            if (lines.length >= 5) {
                questions.push({
                    id: index,
                    question: lines[0],
                    options: {
                        A: (lines[1] || '').replace(/^[A-D][\.\)]\s*/, ''),
                        B: (lines[2] || '').replace(/^[A-D][\.\)]\s*/, ''),
                        C: (lines[3] || '').replace(/^[A-D][\.\)]\s*/, ''),
                        D: (lines[4] || '').replace(/^[A-D][\.\)]\s*/, '')
                    },
                    correctAnswer: 'A',
                    explanation: lines.length > 5 ? lines.slice(5).join(' ') : 'See explanation above.',
                    upscRelevance: ''
                });
            }
        }
    });

    return questions;
}

function renderQuestion() {
    if (quizQuestions.length === 0) return;

    const q = quizQuestions[currentQuestionIndex];
    const total = quizQuestions.length;

    // Update progress
    const progress = ((currentQuestionIndex + 1) / total) * 100;
    document.getElementById('quizProgressFill').style.width = progress + '%';
    document.getElementById('quizProgressText').textContent =
        `Question ${currentQuestionIndex + 1} / ${total}`;

    // Render question
    const container = document.getElementById('questionContainer');
    const selectedAnswer = userAnswers[q.id];

    container.innerHTML = `
        <div class="quiz-question">
            <h3 class="question-text">${currentQuestionIndex + 1}. ${q.question}</h3>
            <div class="options-grid">
                ${Object.entries(q.options).map(([key, value]) => `
                    <button class="option-btn ${selectedAnswer === key ? 'selected' : ''}"
                            onclick="selectAnswer('${q.id}', '${key}')">
                        <span class="option-key">${key}</span>
                        <span class="option-text">${value}</span>
                    </button>
                `).join('')}
            </div>
        </div>
    `;

    // Update navigation buttons
    document.getElementById('prevQuestionBtn').disabled = currentQuestionIndex === 0;

    if (currentQuestionIndex === total - 1) {
        document.getElementById('nextQuestionBtn').style.display = 'none';
        document.getElementById('submitQuizBtn').style.display = 'inline-block';
    } else {
        document.getElementById('nextQuestionBtn').style.display = 'inline-block';
        document.getElementById('submitQuizBtn').style.display = 'none';
    }
}

function selectAnswer(questionId, answer) {
    userAnswers[questionId] = answer;
    renderQuestion(); // Re-render to show selected state
}

function nextQuestion() {
    if (currentQuestionIndex < quizQuestions.length - 1) {
        currentQuestionIndex++;
        renderQuestion();
    }
}

function prevQuestion() {
    if (currentQuestionIndex > 0) {
        currentQuestionIndex--;
        renderQuestion();
    }
}

async function submitQuiz() {
    const total = quizQuestions.length;
    let correct = 0;

    // Calculate score
    const reviewHTML = quizQuestions.map((q, index) => {
        const userAnswer = userAnswers[q.id] || 'Not answered';
        const isCorrect = userAnswer === q.correctAnswer;
        if (isCorrect) correct++;

        return `
            <div class="review-question ${isCorrect ? 'correct' : 'incorrect'}">
                <div class="review-header">
                    <span class="review-number">Q${index + 1}</span>
                    <span class="review-status">${isCorrect ? '✅ Correct' : '❌ Incorrect'}</span>
                </div>
                <p class="review-question-text">${q.question}</p>
                <div class="review-answers">
                    ${Object.entries(q.options).map(([key, value]) => {
            let className = 'review-option';
            if (key === q.correctAnswer) className += ' correct-answer';
            if (key === userAnswer && !isCorrect) className += ' wrong-answer';
            return `<div class="${className}">
                            <span class="option-key">${key}</span> ${value}
                            ${key === q.correctAnswer ? ' ✅' : ''}
                            ${key === userAnswer && !isCorrect ? ' ❌ (Your answer)' : ''}
                        </div>`;
        }).join('')}
                </div>
                <div class="review-explanation">
                    <strong>📖 Explanation:</strong> ${q.explanation}
                </div>
                ${q.upscRelevance ? `
                    <div class="review-relevance">
                        <strong>🎯 UPSC Relevance:</strong> ${q.upscRelevance}
                    </div>
                ` : ''}
            </div>
        `;
    }).join('');

    const percentage = Math.round((correct / total) * 100);

    // Show results
    document.getElementById('quizArea').style.display = 'none';
    document.getElementById('quizResults').style.display = 'block';

    document.getElementById('finalScore').textContent = `${correct}/${total}`;
    document.getElementById('finalPercentage').textContent = `${percentage}%`;
    document.getElementById('quizReview').innerHTML = reviewHTML;

    // Get performance emoji
    let emoji, message;
    if (percentage >= 80) { emoji = '🏆'; message = 'Excellent! You\'re well-prepared!'; }
    else if (percentage >= 60) { emoji = '👍'; message = 'Good job! Keep practicing!'; }
    else if (percentage >= 40) { emoji = '📚'; message = 'Need more revision. Don\'t give up!'; }
    else { emoji = '💪'; message = 'Focus on basics. Every topper started here!'; }

    document.getElementById('quizFeedback').innerHTML = `
        <div class="feedback-banner ${percentage >= 60 ? 'positive' : 'needs-work'}">
            <span class="feedback-emoji">${emoji}</span>
            <span class="feedback-message">${message}</span>
        </div>
    `;

    // Submit results to backend
    try {
        const user = JSON.parse(localStorage.getItem('upscMentorUser'));
        await fetch('/api/quiz/submit?' + new URLSearchParams({
            userId: user.id,
            correctAnswers: correct,
            totalQuestions: total
        }), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: user.id,
                subject: document.getElementById('quizSubject').value,
                difficulty: document.getElementById('quizDifficulty').value,
                numberOfQuestions: total
            })
        });
    } catch (e) {
        console.log('Could not save quiz results:', e);
    }
}

function retakeQuiz() {
    resetQuiz();
}

function resetQuiz() {
    quizQuestions = [];
    currentQuestionIndex = 0;
    userAnswers = {};

    document.getElementById('quizSetup').style.display = 'block';
    document.getElementById('quizArea').style.display = 'none';
    document.getElementById('quizResults').style.display = 'none';
    document.getElementById('quizLoading').style.display = 'none';
}