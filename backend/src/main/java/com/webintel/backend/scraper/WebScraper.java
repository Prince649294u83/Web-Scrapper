package com.webintel.backend.scraper;

import com.webintel.backend.dto.ScrapeResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebScraper {

    public List<ScrapeResult> scrape(String url, String selector) throws Exception {

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        String pageTitle = doc.title();
        List<ScrapeResult> results = new ArrayList<>();

        Elements elements;

        // ✅ IF SELECTOR EMPTY → SCRAPE WHOLE PAGE
        if (selector == null || selector.isBlank()) {
            elements = doc.select("h1, h2, h3, p, li");
        } else {
            elements = doc.select(selector);
        }

        int index = 1;
        for (Element el : elements) {
            String text = el.text().trim();
            if (!text.isEmpty()) {
                results.add(new ScrapeResult(
                        index++,
                        el.tagName(),
                        text,
                        pageTitle
                ));
            }
        }

        return results;
    }
}
