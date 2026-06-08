package com.upscmentor.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChromaDbClient {

    private static final Logger logger = LoggerFactory.getLogger(ChromaDbClient.class);
    private final ChromaDbConfig config;
    private final ObjectMapper mapper;
    private String collectionId;

    public ChromaDbClient(ChromaDbConfig config) {
        this.config = config;
        this.mapper = new ObjectMapper();
        resolveCollectionId();
    }

    private void resolveCollectionId() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(config.getUrl() + "/api/v1/collections/" + config.getCollection());
            collectionId = client.execute(get, response -> {
                String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines().reduce("", (a, b) -> a + b);
                return mapper.readTree(json).get("id").asText();
            });
            logger.info("ChromaDB collection '{}' resolved, id={}", config.getCollection(), collectionId);
        } catch (Exception e) {
            logger.warn("ChromaDB not available at {} or collection '{}' not found. RAG features will be disabled when ChromaDB is offline.", config.getUrl(), config.getCollection());
            collectionId = null;
        }
    }

    private String ensureCollection() {
        if (collectionId != null) return collectionId;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Try to get existing collection
            HttpGet get = new HttpGet(config.getUrl() + "/api/v1/collections/" + config.getCollection());
            try {
                collectionId = client.execute(get, response -> {
                    String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                            .lines().reduce("", (a, b) -> a + b);
                    return mapper.readTree(json).get("id").asText();
                });
                return collectionId;
            } catch (Exception ignored) {
                // Collection doesn't exist, create it
            }

            HttpPost post = new HttpPost(config.getUrl() + "/api/v1/collections");
            ObjectNode body = mapper.createObjectNode();
            body.put("name", config.getCollection());
            body.set("metadata", mapper.createObjectNode().put("type", "upsc_knowledge"));
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            collectionId = client.execute(post, response -> {
                String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines().reduce("", (a, b) -> a + b);
                return mapper.readTree(json).get("id").asText();
            });
            logger.info("Created ChromaDB collection: {}", config.getCollection());
            return collectionId;
        } catch (Exception e) {
            logger.error("Failed to resolve/create ChromaDB collection: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Add embeddings to ChromaDB.
     */
    public boolean addEmbeddings(List<String> ids, List<float[]> embeddings,
                                  List<String> documents, List<ObjectNode> metadatas) {
        String collId = ensureCollection();
        if (collId == null || ids.isEmpty()) return false;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(config.getUrl() + "/api/v1/collections/" + collId + "/add");
            ObjectNode body = mapper.createObjectNode();

            ArrayNode idsArray = mapper.createArrayNode();
            ids.forEach(idsArray::add);
            body.set("ids", idsArray);

            ArrayNode embeddingsArray = mapper.createArrayNode();
            for (float[] emb : embeddings) {
                ArrayNode embArray = mapper.createArrayNode();
                for (float v : emb) embArray.add(v);
                embeddingsArray.add(embArray);
            }
            body.set("embeddings", embeddingsArray);

            ArrayNode docsArray = mapper.createArrayNode();
            documents.forEach(docsArray::add);
            body.set("documents", docsArray);

            ArrayNode metaArray = mapper.createArrayNode();
            metadatas.forEach(metaArray::add);
            body.set("metadatas", metaArray);

            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> response.getCode() == 200 || response.getCode() == 201);
        } catch (Exception e) {
            logger.error("Failed to add embeddings to ChromaDB: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Query ChromaDB with an embedding, filtering by user_id.
     */
    public List<Result> query(float[] queryEmbedding, Long userId, int nResults) {
        String collId = ensureCollection();
        if (collId == null) return List.of();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(config.getUrl() + "/api/v1/collections/" + collId + "/query");
            ObjectNode body = mapper.createObjectNode();

            ArrayNode embArray = mapper.createArrayNode();
            for (float v : queryEmbedding) embArray.add(v);
            body.set("query_embeddings", mapper.createArrayNode().add(embArray));
            body.put("n_results", nResults);

            ObjectNode where = mapper.createObjectNode();
            where.put("user_id", String.valueOf(userId));
            body.set("where", where);

            body.set("include", mapper.createArrayNode().add("documents").add("metadatas").add("distances"));

            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> {
                String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines().reduce("", (a, b) -> a + b);
                JsonNode root = mapper.readTree(json);
                List<Result> results = new ArrayList<>();

                if (root.has("documents") && root.get("documents").isArray() && root.get("documents").size() > 0) {
                    JsonNode docs = root.get("documents").get(0);
                    JsonNode metas = root.get("metadatas") != null && root.get("metadatas").isArray() && root.get("metadatas").size() > 0
                            ? root.get("metadatas").get(0) : null;
                    JsonNode distances = root.get("distances") != null && root.get("distances").isArray() && root.get("distances").size() > 0
                            ? root.get("distances").get(0) : null;

                    for (int i = 0; i < docs.size(); i++) {
                        Result r = new Result();
                        r.document = docs.get(i).asText();
                        if (metas != null && metas.isArray() && i < metas.size()) {
                            r.metadata = (ObjectNode) metas.get(i);
                        }
                        if (distances != null && distances.isArray() && i < distances.size()) {
                            r.distance = distances.get(i).asDouble();
                        }
                        results.add(r);
                    }
                }
                return results;
            });
        } catch (Exception e) {
            logger.error("Failed to query ChromaDB: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Delete all embeddings for a specific document.
     */
    public boolean deleteByDocumentId(Long documentId) {
        String collId = ensureCollection();
        if (collId == null) return false;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(config.getUrl() + "/api/v1/collections/" + collId + "/delete");
            ObjectNode body = mapper.createObjectNode();
            ObjectNode where = mapper.createObjectNode();
            where.put("document_id", String.valueOf(documentId));
            body.set("where", where);

            post.setEntity(new StringEntity(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));

            return client.execute(post, response -> response.getCode() == 200);
        } catch (Exception e) {
            logger.error("Failed to delete from ChromaDB: {}", e.getMessage());
            return false;
        }
    }

    public static class Result {
        public String document;
        public ObjectNode metadata;
        public double distance;
    }
}
