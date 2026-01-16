package com.webintel.backend.packaging;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Packages a directory into a ZIP archive.
 */
public class ZipPackager {

    public static File zipDirectory(File sourceDir) {

        File zipFile = new File(
                sourceDir.getParent(),
                sourceDir.getName() + ".zip"
        );

        try (ZipOutputStream zos =
                     new ZipOutputStream(new FileOutputStream(zipFile))) {

            zipRecursive(sourceDir, sourceDir.getName(), zos);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create ZIP", e);
        }

        return zipFile;
    }

    private static void zipRecursive(
            File file,
            String entryName,
            ZipOutputStream zos
    ) throws Exception {

        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                zipRecursive(
                        child,
                        entryName + "/" + child.getName(),
                        zos
                );
            }
        } else {
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(file.toPath(), zos);
            zos.closeEntry();
        }
    }
}
