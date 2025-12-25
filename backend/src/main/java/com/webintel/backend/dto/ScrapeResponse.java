package com.webintel.backend.dto;

import java.util.List;

public class ScrapeResponse {
    public List<String> results;

    public ScrapeResponse(List<String> results) {
        this.results = results;
    }
}
