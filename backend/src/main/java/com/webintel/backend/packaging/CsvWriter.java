package com.webintel.backend.packaging;

import com.webintel.backend.domain.CrawlResult;
import com.webintel.backend.domain.PageResult;

import java.util.List;

/**
 * Converts CrawlResult into a production-grade,
 * Excel / Sheets / BI-friendly CSV format.
 */
public class CsvWriter {

    /**
     * Converts CrawlResult into a safe, structured CSV.
     */
    public static String toCsv(CrawlResult result) {

        StringBuilder csv = new StringBuilder();

        // ✅ UTF-8 BOM (prevents Excel encoding issues)
        csv.append("\uFEFF");

        /* =========================
           AI SUMMARY (HUMAN SECTION)
           ========================= */
        if (result.getSummary() != null && !result.getSummary().isBlank()) {
            csv.append("AI SUMMARY\n");
            csv.append(escape(result.getSummary())).append("\n\n");
        }

        /* =========================
           TABLE HEADER
           ========================= */
        csv.append(
                "Page Index," +
                        "Page URL," +
                        "Content Type," +
                        "Content Index," +
                        "Content," +
                        "Page Items Total," +
                        "Truncated," +
                        "Truncation Reason," +
                        "Error\n"
        );

        List<PageResult> pages = result.getPages();

        if (pages == null || pages.isEmpty()) {
            return csv.toString();
        }

        int pageIndex = 0;

        for (PageResult page : pages) {

            pageIndex++;

            int contentIndex;

            /* =========================
               HEADINGS
               ========================= */
            contentIndex = 0;
            if (page.getHeadings() != null) {
                for (String h : page.getHeadings()) {
                    contentIndex++;
                    appendRow(
                            csv,
                            pageIndex,
                            page.getPageUrl(),
                            "Heading",
                            contentIndex,
                            h,
                            page
                    );
                }
            }

            /* =========================
               PARAGRAPHS
               ========================= */
            contentIndex = 0;
            if (page.getParagraphs() != null) {
                for (String p : page.getParagraphs()) {
                    contentIndex++;
                    appendRow(
                            csv,
                            pageIndex,
                            page.getPageUrl(),
                            "Paragraph",
                            contentIndex,
                            p,
                            page
                    );
                }
            }

            /* =========================
               LINKS
               ========================= */
            contentIndex = 0;
            if (page.getLinks() != null) {
                for (String l : page.getLinks()) {
                    contentIndex++;
                    appendRow(
                            csv,
                            pageIndex,
                            page.getPageUrl(),
                            "Link",
                            contentIndex,
                            l,
                            page
                    );
                }
            }

            /* =========================
               IMAGES
               ========================= */
            contentIndex = 0;
            if (page.getImages() != null) {
                for (String img : page.getImages()) {
                    contentIndex++;
                    appendRow(
                            csv,
                            pageIndex,
                            page.getPageUrl(),
                            "Image",
                            contentIndex,
                            img,
                            page
                    );
                }
            }

            /* =========================
               ERROR ROW (IF ANY)
               ========================= */
            if (page.getError() != null && !page.getError().isBlank()) {
                appendRow(
                        csv,
                        pageIndex,
                        page.getPageUrl(),
                        "ERROR",
                        0,
                        page.getError(),
                        page
                );
            }
        }

        return csv.toString();
    }

    /* ============================================================
       HELPERS
       ============================================================ */

    private static void appendRow(
            StringBuilder csv,
            int pageIndex,
            String pageUrl,
            String type,
            int contentIndex,
            String content,
            PageResult page
    ) {
        csv.append(pageIndex).append(",");
        csv.append(escape(pageUrl)).append(",");
        csv.append(type).append(",");
        csv.append(contentIndex).append(",");
        csv.append(escape(content)).append(",");
        csv.append(page.getTotalItems()).append(",");
        csv.append(page.isTruncated()).append(",");
        csv.append(escape(page.getTruncationReason())).append(",");
        csv.append(escape(page.getError())).append("\n");
    }

    /**
     * Escapes CSV values safely.
     */
    private static String escape(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
