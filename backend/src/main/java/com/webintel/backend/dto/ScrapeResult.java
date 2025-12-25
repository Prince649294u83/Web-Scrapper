package com.webintel.backend.dto;

public class ScrapeResult {

    private int index;
    private String tag;
    private String text;
    private String pageTitle;
    private String timestamp;

    public ScrapeResult(int index, String tag, String text, String pageTitle, String timestamp) {
        this.index = index;
        this.tag = tag;
        this.text = text;
        this.pageTitle = pageTitle;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }
}
