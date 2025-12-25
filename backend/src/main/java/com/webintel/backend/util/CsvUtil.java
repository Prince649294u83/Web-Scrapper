package com.webintel.backend.util;

import com.webintel.backend.dto.ScrapeResult;

import java.util.List;

public class CsvUtil {

    public static String generateCsv(List<ScrapeResult> results) {

        StringBuilder sb = new StringBuilder();
        sb.append("Index,Tag,Text,Page Title,Timestamp\n");

        for (ScrapeResult r : results) {
            sb.append(r.getIndex()).append(",")
                    .append(r.getTag()).append(",")
                    .append("\"").append(r.getText().replace("\"", "\"\"")).append("\",")
                    .append("\"").append(r.getPageTitle()).append("\",")
                    .append(r.getTimestamp()).append("\n");
        }

        return sb.toString();
    }
}
