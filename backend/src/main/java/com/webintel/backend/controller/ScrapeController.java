package com.webintel.backend.controller;

import com.webintel.backend.dto.ScrapeRequest;
import com.webintel.backend.dto.ScrapeResult;
import com.webintel.backend.service.ScrapeService;
import com.webintel.backend.util.CsvUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrape")
@CrossOrigin(origins = "*")
public class ScrapeController {

    private final ScrapeService scrapeService;

    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    // ✅ MAIN SCRAPE ENDPOINT (FOR TABLE)
    @PostMapping
    public List<ScrapeResult> scrape(@RequestBody ScrapeRequest request) {
        return scrapeService.scrape(request);
    }

    // ✅ CSV DOWNLOAD
    @PostMapping("/csv")
    public ResponseEntity<byte[]> downloadCsv(@RequestBody ScrapeRequest request) {

        List<ScrapeResult> results = scrapeService.scrape(request);
        String csv = CsvUtil.generateCsv(results);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"scrape-results.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.getBytes());
    }

    // 🤖 AI SUMMARY
    @PostMapping("/summary")
    public ResponseEntity<String> generateSummary(@RequestBody ScrapeRequest request) {
        String summary = scrapeService.generateSummary(request);
        return ResponseEntity.ok(summary);
    }
}
