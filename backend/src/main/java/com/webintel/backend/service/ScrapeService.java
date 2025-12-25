package com.webintel.backend.service;

import com.webintel.backend.dto.ScrapeRequest;
import com.webintel.backend.dto.ScrapeResult;
import com.webintel.backend.scraper.WebScraper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapeService {

    // 🔹 SCRAPE DATA
    public List<ScrapeResult> scrape(ScrapeRequest request) {
        try {
            return WebScraper.scrape(
                    request.getUrl(),
                    request.getSelector()
            );
        } catch (Exception e) {
            throw new RuntimeException("Scraping failed", e);
        }
    }

    // 🤖 AI-LIKE SUMMARY (SAFE VERSION)
    public String generateSummary(ScrapeRequest request) {
        try {
            List<ScrapeResult> results =
                    WebScraper.scrape(
                            request.getUrl(),
                            request.getSelector()
                    );

            if (results.isEmpty()) {
                return "No content was found on the page for the given selector.";
            }

            String pageTitle = results.get(0).getPageTitle();
            int count = results.size();

            String combinedText = results.stream()
                    .map(ScrapeResult::getText)
                    .limit(5)
                    .collect(Collectors.joining(" | "));

            return """
                    Page Title: %s

                    Total elements scraped: %d

                    Content Overview:
                    %s
                    """.formatted(pageTitle, count, combinedText);

        } catch (Exception e) {
            return "AI Summary generation failed due to scraping error.";
        }
    }
}
