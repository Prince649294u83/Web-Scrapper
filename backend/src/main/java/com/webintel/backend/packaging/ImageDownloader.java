package com.webintel.backend.packaging;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Downloads images from URLs and stores them locally.
 */
public class ImageDownloader {

    public static void download(List<String> imageUrls, Path outputDir) {

        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create image directory", e);
        }

        int index = 1;

        for (String imageUrl : imageUrls) {
            try (InputStream in = new URL(imageUrl).openStream()) {

                String extension = imageUrl.contains(".")
                        ? imageUrl.substring(imageUrl.lastIndexOf("."))
                        : ".jpg";

                Path filePath =
                        outputDir.resolve("image_" + index++ + extension);

                Files.copy(in, filePath);

            } catch (Exception ignored) {
                // Skip broken images safely
            }
        }
    }
}
