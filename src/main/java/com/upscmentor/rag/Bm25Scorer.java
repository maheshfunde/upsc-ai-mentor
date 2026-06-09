package com.upscmentor.rag;

import java.util.*;

public class Bm25Scorer {

    private static final double K1 = 1.5;
    private static final double B = 0.75;

    public List<Double> score(String query, List<String> documents) {
        List<String> queryTerms = tokenize(query);
        List<List<String>> docTokens = new ArrayList<>();
        for (String doc : documents) {
            docTokens.add(tokenize(doc));
        }

        int N = documents.size();
        List<Double> scores = new ArrayList<>();
        double avgDocLen = docTokens.stream().mapToInt(List::size).average().orElse(1);

        for (int d = 0; d < N; d++) {
            List<String> doc = docTokens.get(d);
            double score = 0;
            long docLen = doc.size();

            for (String term : queryTerms) {
                long tf = doc.stream().filter(t -> t.equals(term)).count();
                long df = docTokens.stream().filter(dt -> dt.contains(term)).count();

                double idf = Math.log((double) (N - df + 0.5) / (df + 0.5) + 1.0);
                double termScore = idf * (tf * (K1 + 1)) / (tf + K1 * (1 - B + B * (docLen / avgDocLen)));
                score += termScore;
            }

            scores.add(score);
        }

        return scores;
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[\\s\\p{Punct}]+"))
                .filter(t -> t.length() > 1)
                .toList();
    }
}
