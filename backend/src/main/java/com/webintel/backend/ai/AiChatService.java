package com.webintel.backend.ai;

import com.webintel.backend.domain.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiChatService {

    private final LocalAiClient ai;

    public AiChatService(LocalAiClient ai) {
        this.ai = ai;
    }

    public String chat(String question, List<PageResult> pages) {

        StringBuilder context = new StringBuilder();

        for (PageResult page : pages) {
            if (page.getHeadings() != null) {
                page.getHeadings().forEach(h -> context.append(h).append("\n"));
            }
            if (page.getParagraphs() != null) {
                page.getParagraphs().forEach(p -> context.append(p).append("\n"));
            }
        }

        String prompt = """
        Answer the question using ONLY the information below.
        If the answer is not present, say so clearly.

        Question:
        %s

        Context:
        %s
        """.formatted(question, context);

        return ai.generate(prompt);
    }
}
