package com.webintel.backend.util;

import com.webintel.backend.dto.ScrapeResult;

import java.util.List;

public class CsvUtil {

    public static String convertToCsv(List<ScrapeResult> results) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Index,Tag,Text,Page Title,Timestamp\n");

        // Rows
        for (ScrapeResult result : results) {
            csv.append(result.getIndex()).append(",");
            csv.append(escape(result.getTag())).append(",");
            csv.append(escape(result.getText())).append(",");
            csv.append(escape(result.getPageTitle())).append(",");
            csv.append(result.getTimestamp()).append("\n");
        }

        return csv.toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
