package com.upscmentor.model.dto;

import com.upscmentor.model.enums.Subject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 5000)
    private String message;

    private Subject subject;
    private String sessionId;
    private Long userId;
    private boolean isOptionalSubject;

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isOptionalSubject() { return isOptionalSubject; }
    public void setOptionalSubject(boolean optionalSubject) { isOptionalSubject = optionalSubject; }
}