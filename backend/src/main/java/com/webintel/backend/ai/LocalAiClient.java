package com.webintel.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class LocalAiClient {

    // =========================
    // CONFIG
    // =========================
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "llama3";

    // Hard safety limits
    private static final int MAX_PROMPT_CHARS = 12000;
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final HttpClient client;
    private final ObjectMapper mapper;

    public LocalAiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Sends a prompt to a local Ollama / LLaMA model.
     * Fully safe for large content and UI stability.
     */
    public String generate(String prompt) {

        if (prompt == null || prompt.isBlank()) {
            return "";
        }

        // 🔒 Hard trim to protect Ollama & JVM
        String safePrompt = prompt.length() > MAX_PROMPT_CHARS
                ? prompt.substring(0, MAX_PROMPT_CHARS)
                : prompt;

        try {
            String body = """
            {
              "model": "%s",
              "prompt": "%s",
              "stream": false,
              "options": {
                "temperature": 0.2,
                "top_p": 0.9,
                "num_ctx": 4096
              }
            }
            """.formatted(
                    MODEL,
                    escapeJson(safePrompt)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 || response.body() == null) {
                return "";
            }

            JsonNode json = mapper.readTree(response.body());

            String output = json.path("response").asText("");

            // 🔧 Clean & normalize output
            return output
                    .replaceAll("[\\r\\n]{3,}", "\n\n")
                    .trim();

        } catch (Exception e) {
            // Never crash backend
            return "";
        }
    }

    /**
     * Escapes prompt safely for JSON.
     */
    private String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
