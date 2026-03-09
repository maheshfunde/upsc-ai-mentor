package com.upscmentor.model.dto;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String sessionId;
    private String subject;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;

    public static ChatResponse success(String message, String sessionId, String subject) {
        ChatResponse response = new ChatResponse();
        response.message = message;
        response.sessionId = sessionId;
        response.subject = subject;
        response.timestamp = LocalDateTime.now();
        response.success = true;
        return response;
    }

    public static ChatResponse error(String errorMessage) {
        ChatResponse response = new ChatResponse();
        response.error = errorMessage;
        response.timestamp = LocalDateTime.now();
        response.success = false;
        return response;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSuccess() { return success; }
    public String getError() { return error; }
}