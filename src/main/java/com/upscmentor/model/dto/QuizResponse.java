package com.upscmentor.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizResponse {

    private boolean success;
    private String error;
    private LocalDateTime timestamp;
    private QuizData quizData;

    // ===== Inner Classes =====

    /**
     * Represents the complete quiz with all questions
     */
    public static class QuizData {
        private String subject;
        private String topic;
        private String difficulty;
        private int totalQuestions;
        private List<QuizQuestion> questions;

        // Getters and Setters
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public List<QuizQuestion> getQuestions() { return questions; }
        public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
    }

    /**
     * Represents a single quiz question with options
     */
    public static class QuizQuestion {
        private int id;
        private String question;
        private QuizOptions options;
        private String correctAnswer;    // "A", "B", "C", or "D"
        private String explanation;
        private String upscRelevance;
        private String difficulty;
        private String topic;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public QuizOptions getOptions() { return options; }
        public void setOptions(QuizOptions options) { this.options = options; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
        public String getUpscRelevance() { return upscRelevance; }
        public void setUpscRelevance(String upscRelevance) { this.upscRelevance = upscRelevance; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }

    /**
     * Represents the four MCQ options
     */
    public static class QuizOptions {
        private String A;
        private String B;
        private String C;
        private String D;

        // Constructors
        public QuizOptions() {}

        public QuizOptions(String a, String b, String c, String d) {
            this.A = a;
            this.B = b;
            this.C = c;
            this.D = d;
        }

        // Getters and Setters
        public String getA() { return A; }
        public void setA(String a) { A = a; }
        public String getB() { return B; }
        public void setB(String b) { B = b; }
        public String getC() { return C; }
        public void setC(String c) { C = c; }
        public String getD() { return D; }
        public void setD(String d) { D = d; }

        /**
         * Get option by key (A/B/C/D)
         */
        public String getByKey(String key) {
            return switch (key.toUpperCase()) {
                case "A" -> A;
                case "B" -> B;
                case "C" -> C;
                case "D" -> D;
                default -> null;
            };
        }
    }

    /**
     * Represents quiz submission result
     */
    public static class QuizSubmitResult {
        private int totalQuestions;
        private int correctAnswers;
        private double scorePercentage;
        private String grade;
        private String aiFeedback;
        private List<QuestionResult> questionResults;

        // Getters and Setters
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public int getCorrectAnswers() { return correctAnswers; }
        public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
        public double getScorePercentage() { return scorePercentage; }
        public void setScorePercentage(double scorePercentage) { this.scorePercentage = scorePercentage; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getAiFeedback() { return aiFeedback; }
        public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
        public List<QuestionResult> getQuestionResults() { return questionResults; }
        public void setQuestionResults(List<QuestionResult> questionResults) { this.questionResults = questionResults; }

        /**
         * Calculate grade based on percentage
         */
        public static String calculateGrade(double percentage) {
            if (percentage >= 90) return "Outstanding 🏆";
            if (percentage >= 80) return "Excellent 🌟";
            if (percentage >= 70) return "Very Good 👏";
            if (percentage >= 60) return "Good 👍";
            if (percentage >= 50) return "Average 📚";
            if (percentage >= 40) return "Below Average ⚠️";
            return "Needs Improvement 💪";
        }
    }

    /**
     * Result of individual question
     */
    public static class QuestionResult {
        private int questionId;
        private String question;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        private String explanation;

        // Getters and Setters
        public int getQuestionId() { return questionId; }
        public void setQuestionId(int questionId) { this.questionId = questionId; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    // ===== Static Factory Methods =====

    public static QuizResponse success(QuizData quizData) {
        QuizResponse response = new QuizResponse();
        response.success = true;
        response.quizData = quizData;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static QuizResponse error(String errorMessage) {
        QuizResponse response = new QuizResponse();
        response.success = false;
        response.error = errorMessage;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    // ===== Getters and Setters =====
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public QuizData getQuizData() { return quizData; }
    public void setQuizData(QuizData quizData) { this.quizData = quizData; }
}