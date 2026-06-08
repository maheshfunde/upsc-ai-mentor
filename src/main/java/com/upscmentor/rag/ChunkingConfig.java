package com.upscmentor.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.chunking")
public class ChunkingConfig {

    private int size = 1000;
    private int overlap = 200;

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getOverlap() { return overlap; }
    public void setOverlap(int overlap) { this.overlap = overlap; }
}
