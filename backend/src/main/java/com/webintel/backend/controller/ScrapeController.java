package com.webintel.backend.controller;

import com.webintel.backend.dto.ScrapeRequest;
import com.webintel.backend.dto.ScrapeResult;
import com.webintel.backend.service.ScrapeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/scrape")
@CrossOrigin(origins = "*")
public class ScrapeController {

    private final ScrapeService scrapeService;

    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    // 🔹 JSON SCRAPE
    @PostMapping
    public List<ScrapeResult> scrape(@RequestBody ScrapeRequest request) {
        return scrapeService.scrape(request);
    }

    // 🔹 CSV DOWNLOAD
    @PostMapping("/csv")
    public void downloadCsv(
            @RequestBody ScrapeRequest request,
            HttpServletResponse response
    ) throws Exception {

        List<ScrapeResult> results = scrapeService.scrape(request);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=scrape-results.csv");

        PrintWriter writer = response.getWriter();
        writer.println("Index,Tag,Text,PageTitle");

        for (ScrapeResult r : results) {
            writer.printf(
                    "%d,%s,\"%s\",\"%s\"%n",
                    r.getIndex(),
                    r.getTag(),
                    r.getText().replace("\"", "\"\""),
                    r.getPageTitle()
            );
        }

        writer.flush();
    }

    // 🤖 AI SUMMARY
    @PostMapping(value = "/summary", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateSummary(@RequestBody ScrapeRequest request) {
        return scrapeService.generateSummary(request);
    }
}
