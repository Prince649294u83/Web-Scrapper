package com.webintel.backend.ai;

import org.springframework.stereotype.Service;

@Service
public class AiIntentRefiner {

    private final LocalAiClient ai;

    public AiIntentRefiner(LocalAiClient ai) {
        this.ai = ai;
    }

    /**
     * Converts vague user requests into explicit,
     * deterministic extraction instructions.
     *
     * IMPORTANT:
     * - Output MUST be readable by IntentInterpreter
     * - AI failure must NEVER block scraping
     */
    public String refine(String userInput) {

        // Absolute safety fallback
        if (userInput == null || userInput.isBlank()) {
            return "Extract headings and paragraphs from the page.";
        }

        String prompt = """
You are a web scraping instruction generator.

Rewrite the user's request as a SINGLE clear instruction sentence.

STRICT RULES:
- Mention ONLY these content types if needed:
  headings, paragraphs, images, links
- Clearly state if multi-page crawling is required
- Use simple imperative language starting with "Extract"
- NO explanations
- NO bullet points
- NO formatting
- ONE sentence only

GOOD EXAMPLES:
"Extract headings and paragraphs from the page."
"Extract headings, images and links from the entire website."

BAD EXAMPLES:
"Here is what I will do..."
"Sure! I will help you..."
"- Extract headings"

User request:
%s
""".formatted(userInput);

        try {
            String response = ai.generate(prompt);

            // AI failed → fallback safely
            if (response == null || response.isBlank()) {
                return userInput;
            }

            // HARD sanitization (LLMs are noisy)
            return response
                    .replaceAll("[\\r\\n]+", " ")
                    .replaceAll("[\"']", "")
                    .replaceAll("\\s{2,}", " ")
                    .trim();

        } catch (Exception e) {
            // NEVER block scraping due to AI failure
            return userInput;
        }
    }
}
