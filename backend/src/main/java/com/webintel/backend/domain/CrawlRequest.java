package com.webintel.backend.domain;

/**
 * Incoming request from frontend for smart scraping.
 * Carries raw user intent and crawl configuration.
 */
public class CrawlRequest {

    // Target website URL
    private String targetUrl;

    // Natural language user instruction
    private String userPrompt;

    // 🔍 Optional: max crawl depth (default = 1)
    private Integer maxDepth;

    // 📦 Optional: desired output format (csv, pdf later)
    private String outputFormat;

    // 🧠 Optional: whether AI refinement is enabled
    private boolean useAi = true;

    /* =========================
       GETTERS & SETTERS
       ========================= */

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public Integer getMaxDepth() {
        return maxDepth != null ? maxDepth : 1;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public String getOutputFormat() {
        return outputFormat != null ? outputFormat : "csv";
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public boolean isUseAi() {
        return useAi;
    }

    public void setUseAi(boolean useAi) {
        this.useAi = useAi;
    }

    /* =========================
       VALIDATION HELPERS
       ========================= */

    public boolean isValid() {
        return targetUrl != null
                && !targetUrl.isBlank()
                && userPrompt != null
                && !userPrompt.isBlank();
    }

    public boolean wantsMultiPage() {
        return userPrompt != null &&
                (userPrompt.toLowerCase().contains("entire website")
                        || userPrompt.toLowerCase().contains("all pages")
                        || userPrompt.toLowerCase().contains("whole site"));
    }
}
