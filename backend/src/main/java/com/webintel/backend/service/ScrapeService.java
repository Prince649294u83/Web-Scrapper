package com.webintel.backend.service;

import com.webintel.backend.dto.ScrapeRequest;
import com.webintel.backend.dto.ScrapeResult;
import com.webintel.backend.scraper.WebScraper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapeService {

    public List<ScrapeResult> scrape(ScrapeRequest request) {
        try {
            return WebScraper.scrape(request.getUrl(), request.getSelector());
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed: " + e.getMessage());
        }
    }

    public String generateSummary(ScrapeRequest request) {
        List<ScrapeResult> results = scrape(request);

        return results.stream()
                .map(ScrapeResult::getText)
                .distinct()
                .limit(20)
                .collect(Collectors.joining(". ")) + ".";
    }
}
