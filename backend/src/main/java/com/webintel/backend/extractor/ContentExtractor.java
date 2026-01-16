package com.webintel.backend.extractor;

import com.webintel.backend.domain.CrawlIntent;
import com.webintel.backend.domain.PageResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContentExtractor {

    /* =========================
       SAFETY LIMITS (CRITICAL)
       ========================= */
    private static final int MAX_HEADINGS = 50;
    private static final int MAX_PARAGRAPHS = 120;
    private static final int MAX_IMAGES = 60;
    private static final int MAX_LINKS = 150;

    /**
     * Extracts content from a single page.
     * Hard-limited and UI-safe for large websites.
     */
    public PageResult extract(String url, CrawlIntent intent) {

        PageResult result = new PageResult();
        result.setPageUrl(url);

        boolean truncated = false;
        List<String> truncationReasons = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (WebIntelBot)")
                    .timeout(15_000)
                    .get();

            /* =========================
               HEADINGS
               ========================= */
            if (intent.wantsHeadings()) {
                List<String> headings = new ArrayList<>();
                Elements hs = doc.select("h1, h2, h3");

                for (Element h : hs) {
                    if (headings.size() >= MAX_HEADINGS) {
                        truncated = true;
                        truncationReasons.add("headings limited to " + MAX_HEADINGS);
                        break;
                    }

                    String text = h.text();
                    if (!text.isBlank()) {
                        headings.add(text);
                    }
                }
                result.setHeadings(headings);
            }

            /* =========================
               PARAGRAPHS
               ========================= */
            if (intent.wantsParagraphs()) {
                List<String> paragraphs = new ArrayList<>();
                Elements ps = doc.select("p");

                for (Element p : ps) {
                    if (paragraphs.size() >= MAX_PARAGRAPHS) {
                        truncated = true;
                        truncationReasons.add("paragraphs limited to " + MAX_PARAGRAPHS);
                        break;
                    }

                    String text = p.text();
                    if (!text.isBlank() && text.length() > 40) {
                        paragraphs.add(text);
                    }
                }
                result.setParagraphs(paragraphs);
            }

            /* =========================
               IMAGES
               ========================= */
            if (intent.wantsImages()) {
                List<String> images = new ArrayList<>();
                Elements imgs = doc.select("img[src]");

                for (Element img : imgs) {
                    if (images.size() >= MAX_IMAGES) {
                        truncated = true;
                        truncationReasons.add("images limited to " + MAX_IMAGES);
                        break;
                    }

                    String src = img.absUrl("src");
                    if (!src.isBlank()) {
                        images.add(src);
                    }
                }
                result.setImages(images);
            }

            /* =========================
               LINKS
               ========================= */
            if (intent.wantsLinks()) {
                List<String> links = new ArrayList<>();
                Elements as = doc.select("a[href]");

                for (Element a : as) {
                    if (links.size() >= MAX_LINKS) {
                        truncated = true;
                        truncationReasons.add("links limited to " + MAX_LINKS);
                        break;
                    }

                    String href = a.absUrl("href");
                    if (!href.isBlank()) {
                        links.add(href);
                    }
                }
                result.setLinks(links);
            }

            /* =========================
               TRUNCATION NOTICE
               ========================= */
            if (truncated) {
                result.setError(
                        "Content truncated: " + String.join("; ", truncationReasons)
                );
            }

        } catch (Exception e) {
            // Never crash crawl — always return partial result
            result.setError("Failed to extract page: " + e.getMessage());
        }

        return result;
    }
}
