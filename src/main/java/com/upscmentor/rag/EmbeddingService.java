package com.upscmentor.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private final EmbeddingConfig config;
    private final ObjectMapper mapper;

    public EmbeddingService(EmbeddingConfig config) {
        this.config = config;
        this.mapper = new ObjectMapper();
    }

    public float[] embed(String text) {
        String url = config.getBaseUrl() + "/api/embed";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            var body = mapper.createObjectNode();
            body.put("model", config.getModel());
            body.put("input", text);
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> {
                String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines().reduce("", (a, b) -> a + b);
                JsonNode root = mapper.readTree(json);
                JsonNode embeddings = root.get("embeddings");
                if (embeddings == null || !embeddings.isArray() || embeddings.size() == 0) {
                    throw new RuntimeException("No embeddings returned from Ollama");
                }
                JsonNode firstEmbedding = embeddings.get(0);
                float[] result = new float[firstEmbedding.size()];
                for (int i = 0; i < firstEmbedding.size(); i++) {
                    result[i] = (float) firstEmbedding.get(i).asDouble();
                }
                return result;
            });
        } catch (Exception e) {
            logger.error("Failed to generate embedding: {}", e.getMessage());
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    public List<float[]> embedAll(List<String> texts) {
        String url = config.getBaseUrl() + "/api/embed";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            var body = mapper.createObjectNode();
            body.put("model", config.getModel());

            var inputArray = mapper.createArrayNode();
            texts.forEach(inputArray::add);
            body.set("input", inputArray);

            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> {
                String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines().reduce("", (a, b) -> a + b);
                JsonNode root = mapper.readTree(json);
                JsonNode embeddings = root.get("embeddings");

                List<float[]> results = new ArrayList<>();
                for (JsonNode emb : embeddings) {
                    float[] result = new float[emb.size()];
                    for (int i = 0; i < emb.size(); i++) {
                        result[i] = (float) emb.get(i).asDouble();
                    }
                    results.add(result);
                }
                return results;
            });
        } catch (Exception e) {
            logger.error("Failed to batch embed: {}", e.getMessage());
            throw new RuntimeException("Batch embedding failed", e);
        }
    }
}
