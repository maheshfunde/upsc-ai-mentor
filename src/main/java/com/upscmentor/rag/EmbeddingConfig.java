package com.upscmentor.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.embedding")
public class EmbeddingConfig {

    private String model = "nomic-embed-text";
    private String provider = "ollama";
    private String baseUrl = "http://localhost:11434";

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
