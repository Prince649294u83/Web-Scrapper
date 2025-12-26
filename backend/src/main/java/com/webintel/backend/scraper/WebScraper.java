package com.webintel.backend.scraper;

import com.webintel.backend.dto.ScrapeResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WebScraper {

    public static List<ScrapeResult> scrape(String url, String selector) throws Exception {

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();

        String title = doc.title();

        // 👉 WHOLE PAGE MODE
        if (selector == null || selector.isBlank()) {
            selector = "body *";
        }

        Elements elements = doc.select(selector);
        List<ScrapeResult> results = new ArrayList<>();

        int index = 1;
        for (Element el : elements) {
            String text = el.text().trim();
            if (text.isEmpty()) continue;

            results.add(new ScrapeResult(
                    index++,
                    el.tagName(),
                    text,
                    title
            ));
        }

        return results;
    }
}
