package com.upscmentor.model.dto;

public class AnswerEvaluationResponse {

    private int overallScore;        // Out of 10
    private int contentScore;        // Out of 10
    private int structureScore;      // Out of 10
    private int analyticalScore;     // Out of 10
    private String strengths;
    private String weaknesses;
    private String suggestions;
    private String modelAnswer;
    private String dimensionsMissed;
    private boolean success;
    private String error;

    // Getters and Setters
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public int getContentScore() { return contentScore; }
    public void setContentScore(int contentScore) { this.contentScore = contentScore; }
    public int getStructureScore() { return structureScore; }
    public void setStructureScore(int structureScore) { this.structureScore = structureScore; }
    public int getAnalyticalScore() { return analyticalScore; }
    public void setAnalyticalScore(int analyticalScore) { this.analyticalScore = analyticalScore; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    public String getModelAnswer() { return modelAnswer; }
    public void setModelAnswer(String modelAnswer) { this.modelAnswer = modelAnswer; }
    public String getDimensionsMissed() { return dimensionsMissed; }
    public void setDimensionsMissed(String dimensionsMissed) { this.dimensionsMissed = dimensionsMissed; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}