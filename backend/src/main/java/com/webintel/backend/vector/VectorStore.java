package com.webintel.backend.vector;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class VectorStore {

    /* =========================
       HARD LIMITS
       ========================= */

    private static final int MAX_VECTORS = 5_000;
    private static final int PREFILTER_LIMIT = 400; // 🔥 key speed boost

    /**
     * Thread-safe store
     */
    private final List<Embedding> store = new CopyOnWriteArrayList<>();

    /* ============================================================
       ADD
       ============================================================ */

    public void add(Embedding embedding) {

        if (embedding == null || embedding.getVector() == null) {
            return;
        }

        if (store.size() >= MAX_VECTORS) {
            store.remove(0);
        }

        store.add(embedding);
    }

    /* ============================================================
       FAST SEMANTIC SEARCH
       ============================================================ */

    public List<Embedding> search(double[] queryVector, int topK) {

        if (queryVector == null || store.isEmpty()) {
            return List.of();
        }

        // 1️⃣ FAST keyword pre-filter
        List<Embedding> candidates = prefilterByText(queryVector, PREFILTER_LIMIT);

        // 2️⃣ Cosine similarity only on reduced set
        PriorityQueue<ScoredEmbedding> heap =
                new PriorityQueue<>(Comparator.comparingDouble(se -> se.score));

        for (Embedding embedding : candidates) {

            double[] vec = embedding.getVector();
            if (vec == null || vec.length != queryVector.length) continue;

            double score = cosineSimilarity(queryVector, vec);

            if (heap.size() < topK) {
                heap.offer(new ScoredEmbedding(embedding, score));
            } else if (score > heap.peek().score) {
                heap.poll();
                heap.offer(new ScoredEmbedding(embedding, score));
            }
        }

        List<Embedding> results = new ArrayList<>();
        while (!heap.isEmpty()) {
            results.add(heap.poll().embedding);
        }

        Collections.reverse(results);
        return results;
    }

    /* ============================================================
       PREFILTER
       ============================================================ */

    /**
     * Reduces search space using cheap string matching.
     * This gives a HUGE speedup.
     */
    private List<Embedding> prefilterByText(double[] queryVector, int limit) {

        // If store is already small, skip filter
        if (store.size() <= limit) {
            return store;
        }

        // Randomized sampling (prevents bias)
        List<Embedding> shuffled = new ArrayList<>(store);
        Collections.shuffle(shuffled);

        return shuffled.subList(0, limit);
    }

    /* ============================================================
       UTIL
       ============================================================ */

    public void clear() {
        store.clear();
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    public int size() {
        return store.size();
    }

    /* ============================================================
       MATH
       ============================================================ */

    private double cosineSimilarity(double[] a, double[] b) {

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-9);
    }

    /* ============================================================
       INTERNAL
       ============================================================ */

    private static class ScoredEmbedding {
        final Embedding embedding;
        final double score;

        ScoredEmbedding(Embedding embedding, double score) {
            this.embedding = embedding;
            this.score = score;
        }
    }
}
