package com.upscmentor.rag;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HybridRetrievalEngine {

    private static final Logger logger = LoggerFactory.getLogger(HybridRetrievalEngine.class);

    private final ChromaDbClient chromaDbClient;
    private final EmbeddingService embeddingService;
    private final Bm25Scorer bm25Scorer;

    @Value("${rag.retrieval.top-k:15}")
    private int topK;

    @Value("${rag.retrieval.final-k:5}")
    private int finalK;

    @Value("${rag.retrieval.vector-weight:0.6}")
    private double vectorWeight;

    @Value("${rag.retrieval.keyword-weight:0.4}")
    private double keywordWeight;

    public HybridRetrievalEngine(ChromaDbClient chromaDbClient,
                                  EmbeddingService embeddingService,
                                  Bm25Scorer bm25Scorer) {
        this.chromaDbClient = chromaDbClient;
        this.embeddingService = embeddingService;
        this.bm25Scorer = bm25Scorer;
    }

    public List<RetrievalResult> retrieve(String query, Long userId) {
        try {
            float[] queryEmbedding = embeddingService.embed(query);
            List<ChromaDbClient.Result> vectorResults = chromaDbClient.query(queryEmbedding, userId, topK);

            if (vectorResults.isEmpty()) {
                logger.info("No results from ChromaDB for user {}", userId);
                return List.of();
            }

            List<String> documents = vectorResults.stream().map(r -> r.document).toList();
            List<Double> bm25Scores = bm25Scorer.score(query, documents);

            List<Integer> vectorRanks = rankByDistance(vectorResults);
            List<Integer> bm25Ranks = rankByScore(bm25Scores);

            Map<Integer, Double> fusionScores = new HashMap<>();
            for (int i = 0; i < vectorResults.size(); i++) {
                double vRank = vectorRanks.get(i) + 1;
                double bRank = bm25Ranks.get(i) + 1;
                double fused = vectorWeight / vRank + keywordWeight / bRank;
                fusionScores.put(i, fused);
            }

            List<RetrievalResult> results = fusionScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(finalK)
                    .map(entry -> {
                        int idx = entry.getKey();
                        ChromaDbClient.Result r = vectorResults.get(idx);
                        RetrievalResult result = new RetrievalResult();
                        result.content = r.document;
                        result.score = entry.getValue();
                        result.source = r.metadata != null ? r.metadata.get("source").asText() : "";
                        result.page = r.metadata != null && r.metadata.has("page") ? r.metadata.get("page").asText() : "";
                        result.fileType = r.metadata != null ? r.metadata.get("file_type").asText() : "";
                        return result;
                    })
                    .toList();

            logger.info("Retrieved {} chunks for user {} (query: {})", results.size(), userId, query.substring(0, Math.min(50, query.length())));
            return results;

        } catch (Exception e) {
            logger.error("Hybrid retrieval failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Integer> rankByDistance(List<ChromaDbClient.Result> results) {
        List<Integer> ranks = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) ranks.add(i);
        return ranks;
    }

    private List<Integer> rankByScore(List<Double> scores) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) indices.add(i);
        indices.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));
        List<Integer> ranks = new ArrayList<>(Collections.nCopies(scores.size(), 0));
        for (int i = 0; i < indices.size(); i++) {
            ranks.set(indices.get(i), i);
        }
        return ranks;
    }

    public static class RetrievalResult {
        public String content;
        public double score;
        public String source;
        public String page;
        public String fileType;
    }
}
