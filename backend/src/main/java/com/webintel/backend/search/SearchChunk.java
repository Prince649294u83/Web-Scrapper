package com.webintel.backend.search;

public class SearchChunk {

    private final String pageUrl;
    private final String content;
    private final float[] embedding;

    public SearchChunk(String pageUrl, String content, float[] embedding) {
        this.pageUrl = pageUrl;
        this.content = content;
        this.embedding = embedding;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getContent() {
        return content;
    }

    public float[] getEmbedding() {
        return embedding;
    }
}
