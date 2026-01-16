package com.webintel.backend.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

/**
 * Discovers internal pages starting from a base URL.
 *
 * Guarantees:
 * - Same-domain only
 * - Depth-limited crawling
 * - Page-count limits
 * - No infinite loops
 * - Deterministic, stable output
 */
@Component
public class MultiPageCrawler {

    /* =========================
       HARD SAFETY LIMITS
       ========================= */
    private static final int DEFAULT_MAX_PAGES = 10;
    private static final int DEFAULT_MAX_DEPTH = 2;

    /**
     * Crawl using safe defaults.
     */
    public List<String> crawl(String startUrl) {
        return crawl(startUrl, DEFAULT_MAX_PAGES, DEFAULT_MAX_DEPTH);
    }

    /**
     * Crawl using configurable limits.
     */
    public List<String> crawl(String startUrl, int maxPages, int maxDepth) {

        if (startUrl == null || startUrl.isBlank()) {
            return List.of();
        }

        Set<String> visited = new LinkedHashSet<>();
        Queue<CrawlNode> queue = new ArrayDeque<>();
        List<String> result = new ArrayList<>();

        String baseDomain = extractDomain(startUrl);
        if (baseDomain.isEmpty()) {
            return result;
        }

        queue.add(new CrawlNode(canonicalize(startUrl), 0));

        while (!queue.isEmpty() && result.size() < maxPages) {

            CrawlNode node = queue.poll();
            String currentUrl = node.url;
            int depth = node.depth;

            if (visited.contains(currentUrl) || depth > maxDepth) {
                continue;
            }

            visited.add(currentUrl);
            result.add(currentUrl);

            try {
                Document document = Jsoup.connect(currentUrl)
                        .userAgent("WebIntelBot/1.0")
                        .timeout(12_000)
                        .ignoreHttpErrors(true)
                        .get();

                // Collect links first to ensure deterministic ordering
                List<String> discoveredLinks = new ArrayList<>();

                for (Element link : document.select("a[href]")) {

                    String abs = canonicalize(link.attr("abs:href"));

                    if (abs.isEmpty()) continue;
                    if (isJunkLink(abs)) continue;
                    if (!isSameDomain(baseDomain, abs)) continue;
                    if (visited.contains(abs)) continue;

                    discoveredLinks.add(abs);
                }

                // Sort links for deterministic crawl order
                Collections.sort(discoveredLinks);

                for (String next : discoveredLinks) {
                    if (result.size() + queue.size() >= maxPages) break;
                    queue.add(new CrawlNode(next, depth + 1));
                }

            } catch (Exception ignored) {
                // Fail silently — crawler must never crash pipeline
            }
        }

        return result;
    }

    /* ============================================================
       HELPERS
       ============================================================ */

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isSameDomain(String baseDomain, String url) {
        try {
            URI uri = new URI(url);
            return baseDomain.equalsIgnoreCase(uri.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Canonicalizes URLs to prevent duplicates.
     */
    private String canonicalize(String url) {

        if (url == null || url.isBlank()) return "";

        try {
            URI uri = new URI(url.trim());

            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : "http";
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : "";
            String path = uri.getPath() != null ? uri.getPath() : "";

            // Remove trailing slash
            if (path.endsWith("/") && path.length() > 1) {
                path = path.substring(0, path.length() - 1);
            }

            return scheme + "://" + host + path;

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Filters out non-crawlable or useless URLs.
     */
    private boolean isJunkLink(String url) {

        String lower = url.toLowerCase();

        return lower.startsWith("mailto:")
                || lower.startsWith("tel:")
                || lower.contains("logout")
                || lower.contains("login")
                || lower.contains("signup")
                || lower.contains("calendar")
                || lower.contains("utm_")
                || lower.endsWith(".pdf")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".svg")
                || lower.endsWith(".css")
                || lower.endsWith(".js")
                || lower.endsWith(".ico")
                || lower.endsWith(".zip");
    }

    /* ============================================================
       INTERNAL MODEL
       ============================================================ */

    private static class CrawlNode {
        final String url;
        final int depth;

        CrawlNode(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}
