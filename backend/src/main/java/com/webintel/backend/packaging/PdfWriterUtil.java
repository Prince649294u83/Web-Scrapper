package com.webintel.backend.packaging;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.webintel.backend.domain.CrawlResult;
import com.webintel.backend.domain.PageResult;
import com.lowagie.text.pdf.draw.LineSeparator;


import java.io.ByteArrayOutputStream;

/**
 * PDF generator using OpenPDF.
 * Produces a clean, professional, exam-ready report.
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
            Font smallFont = new Font(Font.HELVETICA, 9, Font.ITALIC);

            /* =========================
               TITLE
               ========================= */
            Paragraph title = new Paragraph("WEB INTELLIGENCE REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
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

                    document.add(new Paragraph("PAGE URL:", sectionFont));
                    document.add(new Paragraph(page.getPageUrl(), smallFont));
                    document.add(Chunk.NEWLINE);

                    if (page.getHeadings() != null && !page.getHeadings().isEmpty()) {
                        document.add(new Paragraph("Headings", sectionFont));
                        for (String h : page.getHeadings()) {
                            document.add(new Paragraph("• " + h, textFont));
                        }
                        document.add(Chunk.NEWLINE);
                    }

                    if (page.getParagraphs() != null && !page.getParagraphs().isEmpty()) {
                        document.add(new Paragraph("Paragraphs", sectionFont));
                        for (String p : page.getParagraphs()) {
                            document.add(new Paragraph(p, textFont));
                            document.add(Chunk.NEWLINE);
                        }
                    }

                    if (page.getLinks() != null && !page.getLinks().isEmpty()) {
                        document.add(new Paragraph("Links", sectionFont));
                        for (String l : page.getLinks()) {
                            document.add(new Paragraph(l, smallFont));
                        }
                        document.add(Chunk.NEWLINE);
                    }

                    if (page.getImages() != null && !page.getImages().isEmpty()) {
                        document.add(new Paragraph("Images (URLs)", sectionFont));
                        for (String img : page.getImages()) {
                            document.add(new Paragraph(img, smallFont));
                        }
                        document.add(Chunk.NEWLINE);
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
}
