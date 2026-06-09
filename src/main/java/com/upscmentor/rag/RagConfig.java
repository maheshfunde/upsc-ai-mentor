package com.upscmentor.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.upload")
public class RagConfig {

    private int max_size_mb = 50;
    private String allowed_types = "pdf,txt,md,png,jpg,jpeg";
    private String storage_path = "./data/uploads";

    public int getMaxSizeMb() { return max_size_mb; }
    public void setMaxSizeMb(int maxSizeMb) { this.max_size_mb = maxSizeMb; }
    public String getAllowedTypes() { return allowed_types; }
    public void setAllowedTypes(String allowedTypes) { this.allowed_types = allowedTypes; }
    public String getStoragePath() { return storage_path; }
    public void setStoragePath(String storagePath) { this.storage_path = storagePath; }

    public boolean isTypeAllowed(String extension) {
        return getAllowedTypes().contains(extension.toLowerCase());
    }
}
