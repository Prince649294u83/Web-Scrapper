package com.webintel.backend.vector;

import java.util.Arrays;

/**
 * Represents a single embedded text chunk.
 * Immutable, safe, and optimized for similarity search.
 */
public final class Embedding {

    private final String id;
    private final String text;
    private final double[] vector;

    // Cached norm for fast cosine similarity
    private final double norm;

    public Embedding(String id, String text, double[] vector) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Embedding id cannot be null or empty");
        }

        if (text == null) {
            text = "";
        }

        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Embedding vector cannot be null or empty");
        }

        this.id = id;
        this.text = text;
        this.vector = vector.clone(); // protect immutability
        this.norm = computeNorm(this.vector);
    }

    /* =========================
       GETTERS
       ========================= */

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public double[] getVector() {
        return vector.clone(); // defensive copy
    }

    public double getNorm() {
        return norm;
    }

    public int getDimension() {
        return vector.length;
    }

    /* =========================
       INTERNAL HELPERS
       ========================= */

    private double computeNorm(double[] v) {
        double sum = 0.0;
        for (double x : v) {
            sum += x * x;
        }
        return Math.sqrt(sum);
    }

    /* =========================
       DEBUGGING
       ========================= */

    @Override
    public String toString() {
        return "Embedding{" +
                "id='" + id + '\'' +
                ", textLength=" + text.length() +
                ", dimension=" + vector.length +
                '}';
    }
}
