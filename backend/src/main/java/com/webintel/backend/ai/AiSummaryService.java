package com.webintel.backend.ai;

import org.springframework.stereotype.Service;

@Service
public class AiSummaryService {

    private final LocalAiClient ai;

    // HARD safety limits (frontend + LLM protection)
    private static final int MAX_INPUT_CHARS = 8000;
    private static final int MAX_OUTPUT_CHARS = 1200;

    public AiSummaryService(LocalAiClient ai) {
        this.ai = ai;
    }

    /**
     * Generates a stable, readable AI summary.
     * GUARANTEED not to break UI or freeze rendering.
     */
    public String summarize(String content) {

        if (content == null || content.isBlank()) {
            return "No meaningful text was found to generate a summary.";
        }

        // 🔒 HARD trim input (critical for Ollama + UI)
        String trimmedContent = content.length() > MAX_INPUT_CHARS
                ? content.substring(0, MAX_INPUT_CHARS)
                : content;

        String prompt = """
You are a professional web content summarizer.

TASK:
Summarize the following content clearly and concisely.

STRICT RULES:
- Focus on key ideas and useful information
- Use short paragraphs or bullet points
- Do NOT repeat raw text
- Do NOT add introductions or conclusions
- Do NOT mention AI or summarization

CONTENT:
%s
""".formatted(trimmedContent);

        try {
            String response = ai.generate(prompt);

            // Absolute safety fallback
            if (response == null || response.isBlank()) {
                return "Summary could not be generated.";
            }

            // 🔧 Normalize output for UI stability
            String cleaned = response
                    .replaceAll("[\\r\\n]{3,}", "\n\n")
                    .replaceAll("\\s{2,}", " ")
                    .trim();

            // 🔒 HARD cap output length
            if (cleaned.length() > MAX_OUTPUT_CHARS) {
                cleaned = cleaned.substring(0, MAX_OUTPUT_CHARS) + "...";
            }

            return cleaned;

        } catch (Exception e) {
            return "AI summary generation failed.";
        }
    }
}
