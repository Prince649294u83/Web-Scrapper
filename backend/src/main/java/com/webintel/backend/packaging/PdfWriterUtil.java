package com.webintel.backend.packaging;

import com.webintel.backend.domain.CrawlResult;
import com.webintel.backend.domain.PageResult;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight PDF generator (no external libs).
 * Produces a clean, readable PDF-like document.
 *
 * NOTE:
 * This is intentionally simple and fast.
 * Can be upgraded later to iText/OpenPDF if needed.
 */
public class PdfWriterUtil {

    public static byte[] toPdf(CrawlResult result) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeLine(out, "WEB INTELLIGENCE REPORT");
        writeLine(out, "=======================");
        writeLine(out, "");

        if (result.getSummary() != null) {
            writeLine(out, "AI SUMMARY:");
            writeLine(out, result.getSummary());
            writeLine(out, "");
        }

        if (result.getPages() == null) {
            return out.toByteArray();
        }

        for (PageResult page : result.getPages()) {

            writeLine(out, "PAGE:");
            writeLine(out, page.getPageUrl());
            writeLine(out, "");

            if (page.getHeadings() != null) {
                writeLine(out, "HEADINGS:");
                for (String h : page.getHeadings()) {
                    writeLine(out, "• " + h);
                }
                writeLine(out, "");
            }

            if (page.getParagraphs() != null) {
                writeLine(out, "PARAGRAPHS:");
                for (String p : page.getParagraphs()) {
                    writeLine(out, p);
                    writeLine(out, "");
                }
            }

            if (page.getLinks() != null) {
                writeLine(out, "LINKS:");
                for (String l : page.getLinks()) {
                    writeLine(out, l);
                }
                writeLine(out, "");
            }

            if (page.getImages() != null) {
                writeLine(out, "IMAGES:");
                for (String img : page.getImages()) {
                    writeLine(out, img);
                }
                writeLine(out, "");
            }

            writeLine(out, "----------------------------------------");
            writeLine(out, "");
        }

        return out.toByteArray();
    }

    private static void writeLine(ByteArrayOutputStream out, String text) {
        try {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
        } catch (Exception ignored) {
        }
    }
}
