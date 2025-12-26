package com.webintel.backend.util;

import com.webintel.backend.dto.ScrapeResult;

import java.util.List;

public class CsvUtil {

    public static String toCsv(List<ScrapeResult> results) {

        StringBuilder sb = new StringBuilder();
        sb.append("Index,Tag,Text,PageTitle,Timestamp\n");

        for (ScrapeResult r : results) {
            sb.append(r.getIndex()).append(",");
            sb.append(r.getTag()).append(",");
            sb.append("\"").append(r.getText().replace("\"", "\"\"")).append("\",");
            sb.append("\"").append(r.getPageTitle()).append("\",");
            sb.append(r.getTimestamp()).append("\n");
        }

        return sb.toString();
    }
}
