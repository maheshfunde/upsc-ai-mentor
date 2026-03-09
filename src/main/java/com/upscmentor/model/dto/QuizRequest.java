package com.upscmentor.model.dto;

import com.upscmentor.model.enums.DifficultyLevel;
import com.upscmentor.model.enums.Subject;

public class QuizRequest {

    private Long userId;
    private Subject subject;
    private boolean isOptionalSubject;
    private DifficultyLevel difficulty;
    private int numberOfQuestions;
    private String specificTopic; // e.g., "Fundamental Rights" within Polity

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public boolean isOptionalSubject() { return isOptionalSubject; }
    public void setOptionalSubject(boolean optionalSubject) { isOptionalSubject = optionalSubject; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
    public int getNumberOfQuestions() { return numberOfQuestions; }
    public void setNumberOfQuestions(int numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }
    public String getSpecificTopic() { return specificTopic; }
    public void setSpecificTopic(String specificTopic) { this.specificTopic = specificTopic; }
}