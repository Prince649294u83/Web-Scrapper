package com.webintel.backend.service;

import com.webintel.backend.dto.ScrapeRequest;
import com.webintel.backend.dto.ScrapeResult;
import com.webintel.backend.scraper.WebScraper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrapeService {

    private final WebScraper scraper;

    public ScrapeService(WebScraper scraper) {
        this.scraper = scraper;
    }

    public List<ScrapeResult> scrape(ScrapeRequest request) {
        try {
            return scraper.scrape(request.getUrl(), request.getSelector());
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed");
        }
    }

    public String generateSummary(ScrapeRequest request) {
        List<ScrapeResult> results = scrape(request);

        if (results.isEmpty()) {
            return "No content found to summarize.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Page Title: ").append(results.get(0).getPageTitle()).append("\n\n");

        results.stream()
                .limit(10)
                .forEach(r -> sb.append("- ").append(r.getText()).append("\n"));

        return sb.toString();
    }
}
