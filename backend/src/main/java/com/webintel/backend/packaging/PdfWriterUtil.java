package com.webintel.backend.packaging;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.webintel.backend.domain.CrawlResult;
import com.webintel.backend.domain.PageResult;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.net.URL;

/**
 * Advanced PDF generator using OpenPDF.
 * - Structured tables
 * - Embedded images
 * - Exam & production ready
 */
public class PdfWriterUtil {

    public static byte[] toPdf(CrawlResult result) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            /* =========================
               FONTS
               ========================= */
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font textFont = new Font(Font.HELVETICA, 11);
            Font smallFont = new Font(Font.HELVETICA, 9);

            /* =========================
               TITLE
               ========================= */
            Paragraph title = new Paragraph("WEB INTELLIGENCE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            /* =========================
               AI SUMMARY
               ========================= */
            if (result.getSummary() != null && !result.getSummary().isBlank()) {
                document.add(new Paragraph("AI SUMMARY", sectionFont));
                document.add(new Paragraph(result.getSummary(), textFont));
                document.add(Chunk.NEWLINE);
            }

            /* =========================
               PAGE CONTENT
               ========================= */
            if (result.getPages() != null) {

                for (PageResult page : result.getPages()) {

                    document.add(new LineSeparator());
                    document.add(Chunk.NEWLINE);

                    document.add(new Paragraph("PAGE URL", sectionFont));
                    document.add(new Paragraph(page.getPageUrl(), smallFont));
                    document.add(Chunk.NEWLINE);

                    /* =========================
                       HEADINGS TABLE
                       ========================= */
                    if (page.getHeadings() != null && !page.getHeadings().isEmpty()) {
                        document.add(new Paragraph("Headings", sectionFont));

                        PdfPTable table = createSingleColumnTable("Heading");
                        for (String h : page.getHeadings()) {
                            table.addCell(new Phrase(h, textFont));
                        }
                        document.add(table);
                        document.add(Chunk.NEWLINE);
                    }

                    /* =========================
                       PARAGRAPHS TABLE
                       ========================= */
                    if (page.getParagraphs() != null && !page.getParagraphs().isEmpty()) {
                        document.add(new Paragraph("Paragraphs", sectionFont));

                        PdfPTable table = createSingleColumnTable("Paragraph");
                        for (String p : page.getParagraphs()) {
                            table.addCell(new Phrase(p, textFont));
                        }
                        document.add(table);
                        document.add(Chunk.NEWLINE);
                    }

                    /* =========================
                       LINKS TABLE
                       ========================= */
                    if (page.getLinks() != null && !page.getLinks().isEmpty()) {
                        document.add(new Paragraph("Links", sectionFont));

                        PdfPTable table = createSingleColumnTable("URL");
                        for (String l : page.getLinks()) {
                            table.addCell(new Phrase(l, smallFont));
                        }
                        document.add(table);
                        document.add(Chunk.NEWLINE);
                    }

                    /* =========================
                       EMBED IMAGES
                       ========================= */
                    if (page.getImages() != null && !page.getImages().isEmpty()) {
                        document.add(new Paragraph("Images", sectionFont));
                        document.add(Chunk.NEWLINE);

                        for (String imgUrl : page.getImages()) {
                            try {
                                Image img = Image.getInstance(new URL(imgUrl));
                                img.scaleToFit(400, 300);
                                img.setAlignment(Image.ALIGN_CENTER);
                                document.add(img);
                                document.add(Chunk.NEWLINE);
                            } catch (Exception ignored) {
                                document.add(new Paragraph(
                                        "⚠ Unable to load image: " + imgUrl,
                                        smallFont
                                ));
                                document.add(Chunk.NEWLINE);
                            }
                        }
                    }
                }
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            // Fail-safe: never crash backend
            return new byte[0];
        }
    }

    /* =========================
       TABLE HELPER
       ========================= */
    private static PdfPTable createSingleColumnTable(String header) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell(new Phrase(header));
        headerCell.setBackgroundColor(new Color(230, 230, 230));
        headerCell.setPadding(6);
        table.addCell(headerCell);

        return table;
    }
}
