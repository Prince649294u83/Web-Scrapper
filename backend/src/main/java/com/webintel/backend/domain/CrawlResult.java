package com.webintel.backend.domain;

import java.time.Instant;
import java.util.List;

/**
 * Represents the full result of a crawl operation.
 * Designed for production UI, analytics, CSV export, and AI processing.
 */
public class CrawlResult {

    /* =========================
       CORE DATA
       ========================= */

    // Extracted pages
    private List<PageResult> pages;

    // 🧠 AI-generated summary
    private String summary;

    // 🧠 AI-interpreted intent
    private String interpretedIntent;

    /* =========================
       METADATA (PRODUCTION)
       ========================= */

    // Number of pages scraped
    private int totalPages;

    // Total extracted items across all pages
    private int totalItems;

    // True if ANY page was truncated for safety
    private boolean truncated;

    // ISO-8601 timestamp (immutable)
    private final String generatedAt = Instant.now().toString();

    /* =========================
       GETTERS & SETTERS
       ========================= */

    public List<PageResult> getPages() {
        return pages;
    }

    /**
     * Sets pages and automatically recalculates metadata.
     */
    public void setPages(List<PageResult> pages) {
        this.pages = pages;
        recalculateMetadata();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getInterpretedIntent() {
        return interpretedIntent;
    }

    public void setInterpretedIntent(String interpretedIntent) {
        this.interpretedIntent = interpretedIntent;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    /* =========================
       INTERNAL METADATA LOGIC
       ========================= */

    /**
     * Recalculates all derived metadata safely.
     * This guarantees consistency for UI, CSV, and analytics.
     */
    private void recalculateMetadata() {

        totalPages = pages != null ? pages.size() : 0;
        totalItems = 0;
        truncated = false;

        if (pages == null) {
            return;
        }

        for (PageResult page : pages) {
            if (page == null) continue;

            totalItems += page.getTotalItems();

            if (page.isTruncated()) {
                truncated = true;
            }
        }
    }
}
