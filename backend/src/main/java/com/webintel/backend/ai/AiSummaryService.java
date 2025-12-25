package com.webintel.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AiSummaryService {

    @Value("${ai.api.key}")
    private String apiKey;

    public String summarize(String content) throws Exception {

        String requestBody = """
        {
          "model": "gpt-4o-mini",
          "messages": [
            {
              "role": "system",
              "content": "You summarize scraped web content into clear, concise insights."
            },
            {
              "role": "user",
              "content": "%s"
            }
          ],
          "temperature": 0.3
        }
        """.formatted(content.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Simple extraction (good enough for now)
        String body = response.body();
        int start = body.indexOf("\"content\":\"") + 11;
        int end = body.indexOf("\"", start);

        return body.substring(start, end).replace("\\n", "\n");
    }
}
