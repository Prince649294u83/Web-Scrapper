package com.webintel.backend.ai;

import com.webintel.backend.domain.CrawlIntent;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Converts AI-refined instructions into a strict CrawlIntent.
 * Deterministic, fault-tolerant, and SAFE for production.
 */
@Service
public class IntentInterpreter {

    public CrawlIntent interpret(String prompt) {

        CrawlIntent intent = new CrawlIntent();

        if (prompt == null || prompt.isBlank()) {
            applySafeDefaults(intent, "No prompt provided. Using safe defaults.");
            return intent;
        }

        String text = normalize(prompt);

        /* =========================
           CONTENT TYPE DETECTION
           ========================= */

        Set<String> contentTypes = new LinkedHashSet<>();

        if (containsAny(text, "heading", "headings", "title", "titles")) {
            contentTypes.add("headings");
        }

        if (containsAny(text, "paragraph", "paragraphs", "text", "content", "body", "main")) {
            contentTypes.add("paragraphs");
        }

        if (containsAny(text, "image", "images", "photo", "photos", "picture", "pictures")) {
            contentTypes.add("images");
        }

        if (containsAny(text, "link", "links", "url", "urls", "anchor")) {
            contentTypes.add("links");
        }

        /* =========================
           INTELLIGENT FALLBACK
           ========================= */
        if (contentTypes.isEmpty()) {
            // AI was vague — assume user wants meaningful text
            contentTypes.add("headings");
            contentTypes.add("paragraphs");
        }

        intent.setContentTypes(contentTypes.stream().toList());

        /* =========================
           MULTI-PAGE DETECTION
           ========================= */

        boolean multiPage = containsAny(
                text,
                "entire website",
                "entire site",
                "whole site",
                "all pages",
                "all sections",
                "full website",
                "crawl site",
                "crawl website",
                "from the website",
                "from this site",
                "across pages"
        );

        intent.setMultiPage(multiPage);

        /* =========================
           OUTPUT FORMAT
           ========================= */
        intent.setOutputFormat("csv");

        /* =========================
           EXPLANATION (UI FRIENDLY)
           ========================= */
        intent.setExplanation(buildExplanation(intent));

        return intent;
    }

    /* ============================================================
       HELPERS
       ============================================================ */

    private void applySafeDefaults(CrawlIntent intent, String explanation) {
        intent.setContentTypes(
                Set.of("headings", "paragraphs").stream().toList()
        );
        intent.setMultiPage(false);
        intent.setOutputFormat("csv");
        intent.setExplanation(explanation);
    }

    private String normalize(String input) {
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private String buildExplanation(CrawlIntent intent) {

        StringBuilder sb = new StringBuilder("Extracting ");

        sb.append(String.join(", ", intent.getContentTypes()));

        if (intent.isMultiPage()) {
            sb.append(" from multiple pages of the website.");
        } else {
            sb.append(" from the given page.");
        }

        return sb.toString();
    }
}
