package com.webintel.backend.domain;

import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {

    private static final ConcurrentHashMap<String, CrawlResult> STORE =
            new ConcurrentHashMap<>();

    public static void save(String sessionId, CrawlResult result) {
        STORE.put(sessionId, result);
    }

    public static CrawlResult get(String sessionId) {
        return STORE.get(sessionId);
    }
}
