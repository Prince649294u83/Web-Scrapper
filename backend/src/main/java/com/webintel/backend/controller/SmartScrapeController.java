package com.webintel.backend.controller;

import com.webintel.backend.ai.*;
import com.webintel.backend.crawler.MultiPageCrawler;
import com.webintel.backend.domain.*;
import com.webintel.backend.extractor.ContentExtractor;
import com.webintel.backend.packaging.CsvWriter;
import com.webintel.backend.packaging.PdfWriterUtil;
import com.webintel.backend.vector.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/smart-scrape")
@CrossOrigin(origins = "*")
public class SmartScrapeController {

    /* =========================
       HARD SAFETY LIMITS
       ========================= */
    private static final int MAX_SUMMARY_CHARS = 9_000;
    private static final int MAX_VECTOR_CHUNKS = 1_200;

    /* =========================
       SEARCH CACHE (FAST)
       ========================= */
    private String lastQuery = null;
    private double[] lastQueryVector = null;

    private final IntentInterpreter intentInterpreter;
    private final MultiPageCrawler crawler;
    private final ContentExtractor extractor;
    private final AiSummaryService summaryService;
    private final AiIntentRefiner intentRefiner;

    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final ContentChunker chunker;

    public SmartScrapeController(
            IntentInterpreter intentInterpreter,
            MultiPageCrawler crawler,
            ContentExtractor extractor,
            AiSummaryService summaryService,
            AiIntentRefiner intentRefiner,
            VectorStore vectorStore,
            EmbeddingService embeddingService,
            ContentChunker chunker
    ) {
        this.intentInterpreter = intentInterpreter;
        this.crawler = crawler;
        this.extractor = extractor;
        this.summaryService = summaryService;
        this.intentRefiner = intentRefiner;
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.chunker = chunker;
    }

    /* ============================================================
       PREVIEW — SCRAPE + VECTORIZE + AI SUMMARY
       ============================================================ */

    @PostMapping("/preview")
    public ResponseEntity<CrawlResult> preview(@RequestBody CrawlRequest req) {

        CrawlResult out = new CrawlResult();

        try {
            vectorStore.clear();
            lastQuery = null;
            lastQueryVector = null;

            /* 1️⃣ AI intent refinement */
            String refinedIntent = intentRefiner.refine(req.getUserPrompt());
            out.setInterpretedIntent(refinedIntent);

            CrawlIntent intent = intentInterpreter.interpret(refinedIntent);

            /* 2️⃣ Resolve pages */
            List<String> pages = intent.isMultiPage()
                    ? crawler.crawl(req.getTargetUrl())
                    : List.of(req.getTargetUrl());

            List<PageResult> results = new ArrayList<>();
            StringBuilder summaryText = new StringBuilder();

            int vectorCount = 0;
            int pageIndex = 0;

            /* 3️⃣ Extraction + vector ingestion */
            outer:
            for (String url : pages) {

                PageResult page = extractor.extract(url, intent);
                page.setPageIndex(pageIndex++);
                results.add(page);

                if (page.getParagraphs() == null) continue;

                for (String paragraph : page.getParagraphs()) {

                    if (paragraph == null || paragraph.isBlank()) continue;

                    if (summaryText.length() < MAX_SUMMARY_CHARS) {
                        summaryText.append(paragraph).append("\n");
                    }

                    if (vectorCount >= MAX_VECTOR_CHUNKS) break outer;

                    for (String chunk : chunker.chunk(paragraph)) {

                        if (vectorCount >= MAX_VECTOR_CHUNKS) break outer;
                        if (chunk == null || chunk.isBlank()) continue;

                        double[] vec = embeddingService.embed(chunk);
                        if (vec.length == 0) continue;

                        vectorStore.add(
                                new Embedding(
                                        UUID.randomUUID().toString(),
                                        chunk,
                                        vec
                                )
                        );
                        vectorCount++;
                    }
                }
            }

            out.setPages(results);

            /* 4️⃣ AI SUMMARY */
            out.setSummary(
                    summaryText.isEmpty()
                            ? "No meaningful textual content was found to generate a summary."
                            : summaryService.summarize(summaryText.toString())
            );

            return ResponseEntity.ok(out);

        } catch (Exception e) {
            out.setPages(Collections.emptyList());
            out.setSummary("AI processing failed safely.");
            out.setInterpretedIntent(req.getUserPrompt());
            return ResponseEntity.ok(out);
        }
    }

    /* ============================================================
       SEMANTIC SEARCH — FAST & CACHED
       ============================================================ */

    @PostMapping("/search")
    public ResponseEntity<List<String>> semanticSearch(
            @RequestBody Map<String, String> body) {

        String query = body.get("query");

        if (query == null || query.isBlank() || vectorStore.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        double[] qVec;

        if (query.equalsIgnoreCase(lastQuery) && lastQueryVector != null) {
            qVec = lastQueryVector;
        } else {
            qVec = embeddingService.embed(query);
            lastQuery = query;
            lastQueryVector = qVec;
        }

        if (qVec.length == 0) {
            return ResponseEntity.ok(List.of());
        }

        List<String> matches = vectorStore.search(qVec, 6)
                .stream()
                .map(Embedding::getText)
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(matches);
    }

    /* ============================================================
       CSV EXPORT
       ============================================================ */

    @PostMapping("/export/csv")
    public ResponseEntity<String> exportCsv(@RequestBody CrawlRequest req) {

        CrawlIntent intent =
                intentInterpreter.interpret(
                        intentRefiner.refine(req.getUserPrompt())
                );

        List<String> pages = intent.isMultiPage()
                ? crawler.crawl(req.getTargetUrl())
                : List.of(req.getTargetUrl());

        List<PageResult> results = new ArrayList<>();
        int pageIndex = 0;

        for (String p : pages) {
            PageResult page = extractor.extract(p, intent);
            page.setPageIndex(pageIndex++);
            results.add(page);
        }

        CrawlResult result = new CrawlResult();
        result.setPages(results);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"scraped_data.csv\""
                )
                .contentType(MediaType.TEXT_PLAIN)
                .body(CsvWriter.toCsv(result));
    }

    /* ============================================================
       PDF EXPORT — NEW FEATURE
       ============================================================ */

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody CrawlRequest req) {

        CrawlIntent intent =
                intentInterpreter.interpret(
                        intentRefiner.refine(req.getUserPrompt())
                );

        List<String> pages = intent.isMultiPage()
                ? crawler.crawl(req.getTargetUrl())
                : List.of(req.getTargetUrl());

        List<PageResult> results = new ArrayList<>();
        int pageIndex = 0;

        for (String url : pages) {
            PageResult page = extractor.extract(url, intent);
            page.setPageIndex(pageIndex++);
            results.add(page);
        }

        CrawlResult result = new CrawlResult();
        result.setPages(results);

        byte[] pdf = PdfWriterUtil.toPdf(result);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"web_intelligence_report.pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
