package com.webintel.backend.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates high-quality, deterministic embeddings using Ollama.
 * Optimized with LRU cache for FAST semantic search.
 */
@Service
public class EmbeddingService {

    /* =========================
       CONFIGURATION
       ========================= */

    private static final String OLLAMA_EMBED_URL =
            "http://localhost:11434/api/embeddings";

    private static final String MODEL = "llama3";

    private static final int MAX_INPUT_CHARS = 1_500;
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    // llama3 embedding size
    private static final int EXPECTED_DIMENSION = 4096;

    /* =========================
       EMBEDDING CACHE (LRU)
       ========================= */

    private static final int MAX_CACHE_SIZE = 2_000;

    private final Map<String, double[]> cache =
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, double[]> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };

    private final HttpClient client;
    private final ObjectMapper mapper;

    public EmbeddingService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Generates or retrieves cached embedding.
     * NEVER crashes pipeline.
     */
    public synchronized double[] embed(String text) {

        if (text == null || text.isBlank()) {
            return new double[0];
        }

        String cleaned = normalize(text);

        if (cleaned.length() > MAX_INPUT_CHARS) {
            cleaned = cleaned.substring(0, MAX_INPUT_CHARS);
        }

        String key = Integer.toHexString(cleaned.hashCode());

        /* ===== CACHE HIT ===== */
        double[] cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        /* ===== OLLAMA CALL ===== */
        try {
            String body = """
            {
              "model": "%s",
              "prompt": "%s"
            }
            """.formatted(MODEL, escapeJson(cleaned));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_EMBED_URL))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                return new double[0];
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode embeddingNode = root.path("embedding");

            if (!embeddingNode.isArray()
                    || embeddingNode.size() != EXPECTED_DIMENSION) {
                return new double[0];
            }

            double[] vector = new double[EXPECTED_DIMENSION];
            for (int i = 0; i < EXPECTED_DIMENSION; i++) {
                vector[i] = embeddingNode.get(i).asDouble();
            }

            cache.put(key, vector);
            return vector;

        } catch (Exception e) {
            return new double[0];
        }
    }

    /* =========================
       HELPERS
       ========================= */

    private String normalize(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
