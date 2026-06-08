package com.upscmentor.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.chromadb")
public class ChromaDbConfig {

    private String url = "http://localhost:8000";
    private String collection = "upsc_knowledge";

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
}
