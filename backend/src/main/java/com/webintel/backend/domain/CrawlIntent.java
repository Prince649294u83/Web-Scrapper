package com.webintel.backend.domain;

import java.util.*;

/**
 * Represents the interpreted intent of the user
 * after AI + rule-based understanding.
 *
 * This class MUST always be in a valid, safe state.
 */
public class CrawlIntent {

    /* =========================
       CONSTANTS
       ========================= */

    public static final String TYPE_HEADINGS = "headings";
    public static final String TYPE_PARAGRAPHS = "paragraphs";
    public static final String TYPE_IMAGES = "images";
    public static final String TYPE_LINKS = "links";

    public static final String FORMAT_CSV = "csv";

    /* =========================
       FIELDS
       ========================= */

    // What content to extract
    private final Set<String> contentTypes = new LinkedHashSet<>();

    // Whether crawler should follow internal links
    private boolean multiPage = false;

    // Output format (locked for now)
    private String outputFormat = FORMAT_CSV;

    // Optional AI explanation (frontend display)
    private String explanation;

    // Crawl depth (future-safe)
    private int maxDepth = 1;

    /* =========================
       CONTENT TYPES
       ========================= */

    public List<String> getContentTypes() {
        return List.copyOf(contentTypes);
    }

    public void setContentTypes(List<String> types) {
        contentTypes.clear();

        if (types == null || types.isEmpty()) {
            applySafeDefaults();
            return;
        }

        for (String t : types) {
            normalizeAndAdd(t);
        }

        if (contentTypes.isEmpty()) {
            applySafeDefaults();
        }
    }

    public void addContentType(String type) {
        normalizeAndAdd(type);
    }

    private void normalizeAndAdd(String type) {
        if (type == null) return;

        String t = type.trim().toLowerCase();

        switch (t) {
            case TYPE_HEADINGS,
                 TYPE_PARAGRAPHS,
                 TYPE_IMAGES,
                 TYPE_LINKS -> contentTypes.add(t);
        }
    }

    private void applySafeDefaults() {
        contentTypes.add(TYPE_HEADINGS);
        contentTypes.add(TYPE_PARAGRAPHS);
    }

    /* =========================
       MULTI-PAGE
       ========================= */

    public boolean isMultiPage() {
        return multiPage;
    }

    public void setMultiPage(boolean multiPage) {
        this.multiPage = multiPage;
    }

    /* =========================
       OUTPUT FORMAT
       ========================= */

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String format) {
        // Lock output format for safety
        this.outputFormat = FORMAT_CSV;
    }

    /* =========================
       AI EXPLANATION
       ========================= */

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = (explanation == null || explanation.isBlank())
                ? null
                : explanation.trim();
    }

    /* =========================
       DEPTH
       ========================= */

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = Math.max(1, maxDepth);
    }

    /* =========================
       CONVENIENCE METHODS
       ========================= */

    public boolean wantsImages() {
        return contentTypes.contains(TYPE_IMAGES);
    }

    public boolean wantsHeadings() {
        return contentTypes.contains(TYPE_HEADINGS);
    }

    public boolean wantsParagraphs() {
        return contentTypes.contains(TYPE_PARAGRAPHS);
    }

    public boolean wantsLinks() {
        return contentTypes.contains(TYPE_LINKS);
    }

    public boolean isEmptyIntent() {
        return contentTypes.isEmpty();
    }

    /* =========================
       DEBUG / LOGGING
       ========================= */

    @Override
    public String toString() {
        return "CrawlIntent{" +
                "contentTypes=" + contentTypes +
                ", multiPage=" + multiPage +
                ", outputFormat='" + outputFormat + '\'' +
                ", maxDepth=" + maxDepth +
                '}';
    }
}
