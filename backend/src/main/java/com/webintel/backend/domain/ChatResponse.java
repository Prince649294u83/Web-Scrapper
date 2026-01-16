package com.webintel.backend.domain;

import java.util.List;

public class ChatResponse {

    private String answer;
    private List<String> sources;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
