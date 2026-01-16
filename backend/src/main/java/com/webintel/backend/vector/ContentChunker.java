package com.webintel.backend.vector;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Production-grade content chunker for AI embeddings.
 *
 * Guarantees:
 * - Sentence-aware chunking
 * - Context overlap
 * - Stable embedding quality
 */
@Service
public class ContentChunker {

    // Target size per chunk (characters)
    private static final int TARGET_SIZE = 600;

    // Overlap to preserve context between chunks
    private static final int OVERLAP = 120;

    /**
     * Splits text into semantically meaningful chunks
     * safe for embeddings and summarization.
     */
    public List<String> chunk(String text) {

        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Normalize whitespace
        String cleaned = text
                .replaceAll("\\s+", " ")
                .trim();

        // Sentence-aware split
        String[] sentences = cleaned.split("(?<=[.!?])\\s+");

        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {

            if (current.length() + sentence.length() <= TARGET_SIZE) {
                current.append(sentence).append(" ");
            } else {
                // Save chunk
                chunks.add(current.toString().trim());

                // Start new chunk with overlap
                current = new StringBuilder();

                int overlapStart = Math.max(
                        chunks.get(chunks.size() - 1).length() - OVERLAP,
                        0
                );

                current.append(
                        chunks.get(chunks.size() - 1)
                                .substring(overlapStart)
                ).append(" ");

                current.append(sentence).append(" ");
            }
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}
