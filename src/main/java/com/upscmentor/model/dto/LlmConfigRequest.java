package com.upscmentor.model.dto;

public class LlmConfigRequest {

    private String apiKey;
    private String modelName;
    private String localModelName;
    private String baseUrl;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getLocalModelName() {
        return localModelName;
    }

    public void setLocalModelName(String localModelName) {
        this.localModelName = localModelName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
