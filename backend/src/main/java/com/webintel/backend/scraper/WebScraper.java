package com.webintel.backend.scraper;

import com.webintel.backend.dto.ScrapeResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {

    public static List<ScrapeResult> scrape(String url, String selector) throws Exception {

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        String pageTitle = document.title();
        String timestamp = Instant.now().toString();

        Elements elements = document.select(selector);
        List<ScrapeResult> results = new ArrayList<>();

        int index = 1;
        for (Element element : elements) {
            results.add(new ScrapeResult(
                    index++,
                    element.tagName(),
                    element.text(),
                    pageTitle,
                    timestamp
            ));
        }

        return results;
    }
}
