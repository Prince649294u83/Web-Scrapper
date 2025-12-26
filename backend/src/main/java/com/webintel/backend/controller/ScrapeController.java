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
@CrossOrigin
public class ScrapeController {

    private final ScrapeService service;

    public ScrapeController(ScrapeService service) {
        this.service = service;
    }

    @PostMapping
    public List<ScrapeResult> scrape(@RequestBody ScrapeRequest request) {
        return service.scrape(request);
    }

    @PostMapping("/csv")
    public ResponseEntity<String> csv(@RequestBody ScrapeRequest request) {

        List<ScrapeResult> results = service.scrape(request);
        String csv = CsvUtil.toCsv(results);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=scrape-results.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv);
    }

    @PostMapping("/summary")
    public String summary(@RequestBody ScrapeRequest request) {
        return service.generateSummary(request);
    }
}
