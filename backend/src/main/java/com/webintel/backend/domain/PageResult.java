package com.webintel.backend.domain;

import java.util.List;

/**
 * Represents extracted content from a single web page.
 * Safe for large-scale scraping, UI rendering, tables, and search.
 */
public class PageResult {

    /* =========================
       CORE DATA
       ========================= */

    private String pageUrl;

    private List<String> headings;
    private List<String> paragraphs;
    private List<String> images;
    private List<String> links;

    /* =========================
       METADATA
       ========================= */

    private int pageIndex;

    private int headingsCount;
    private int paragraphsCount;
    private int imagesCount;
    private int linksCount;
    private int totalItems;

    private boolean truncated;
    private String truncationReason;

    private String error;

    /* =========================
       GETTERS / SETTERS
       ========================= */

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public List<String> getHeadings() {
        return headings;
    }

    public void setHeadings(List<String> headings) {
        this.headings = headings;
        this.headingsCount = headings != null ? headings.size() : 0;
        recalcTotal();
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs = paragraphs;
        this.paragraphsCount = paragraphs != null ? paragraphs.size() : 0;
        recalcTotal();
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
        this.imagesCount = images != null ? images.size() : 0;
        recalcTotal();
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
        this.linksCount = links != null ? links.size() : 0;
        recalcTotal();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getHeadingsCount() {
        return headingsCount;
    }

    public int getParagraphsCount() {
        return paragraphsCount;
    }

    public int getImagesCount() {
        return imagesCount;
    }

    public int getLinksCount() {
        return linksCount;
    }

    public int getTotalItems() {
        return totalItems;
    }

    /* =========================
       🔥 MISSING METHODS (FIX)
       ========================= */

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public String getTruncationReason() {
        return truncationReason;
    }

    public void setTruncationReason(String truncationReason) {
        this.truncationReason = truncationReason;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /* =========================
       INTERNAL
       ========================= */

    private void recalcTotal() {
        this.totalItems =
                headingsCount +
                        paragraphsCount +
                        imagesCount +
                        linksCount;
    }
}
