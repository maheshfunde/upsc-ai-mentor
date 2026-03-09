package com.upscmentor.model.dto;

import com.upscmentor.model.enums.Subject;

public class AnswerEvaluationRequest {

    private Long userId;
    private Subject subject;
    private String question;
    private String userAnswer;
    private int wordLimit;
    private boolean isOptionalSubject;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public int getWordLimit() { return wordLimit; }
    public void setWordLimit(int wordLimit) { this.wordLimit = wordLimit; }
    public boolean isOptionalSubject() { return isOptionalSubject; }
    public void setOptionalSubject(boolean optionalSubject) { isOptionalSubject = optionalSubject; }
}