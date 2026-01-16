package com.webintel.backend.packaging;

import com.webintel.backend.domain.CrawlResult;

import java.io.File;
import java.nio.file.Files;

/**
 * Writes crawl results to disk in a structured format.
 */
public class OutputWriter {

    public static File writeResult(File baseDir, CrawlResult result) {

        try {
            File outputFile = new File(baseDir, "content.json");

            Files.writeString(
                    outputFile.toPath(),
                    result.toString()
            );

            return outputFile;

        } catch (Exception e) {
            throw new RuntimeException("Failed to write output", e);
        }
    }
}
