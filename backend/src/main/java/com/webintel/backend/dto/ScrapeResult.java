package com.webintel.backend.dto;

import java.time.LocalDateTime;

public class ScrapeResult {

    private int index;
    private String tag;
    private String text;
    private String pageTitle;
    private LocalDateTime timestamp;

    public ScrapeResult() {}

    public ScrapeResult(int index, String tag, String text, String pageTitle) {
        this.index = index;
        this.tag = tag;
        this.text = text;
        this.pageTitle = pageTitle;
        this.timestamp = LocalDateTime.now();
    }

    public int getIndex() {
        return index;
    }

    public String getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
