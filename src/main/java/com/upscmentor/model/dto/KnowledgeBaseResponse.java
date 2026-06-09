package com.upscmentor.model.dto;

public class KnowledgeBaseResponse {

    private boolean success;
    private String message;
    private Object data;

    public KnowledgeBaseResponse() {}

    public KnowledgeBaseResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static KnowledgeBaseResponse ok(String message, Object data) {
        return new KnowledgeBaseResponse(true, message, data);
    }

    public static KnowledgeBaseResponse error(String message) {
        return new KnowledgeBaseResponse(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
