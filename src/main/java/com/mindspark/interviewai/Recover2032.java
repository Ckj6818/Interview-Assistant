package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class Recover2032 {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        String destDir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";

        String[] targets = {"copilot.html", "resume.html", "records.html", "questions.html", "index.html", "login.html", "register.html", "interview.html", "report.html", "leetcode.html"};
        Map<String, String> latestContent = new HashMap<>();

        // Target time: 2026-05-24T12:35:00Z (20:35 local)
        Instant targetInstant = Instant.parse("2026-05-24T12:35:00Z");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // Check timestamp
            int timeIdx = line.indexOf("\"timestamp\":\"");
            if (timeIdx != -1) {
                int timeEnd = line.indexOf("\"", timeIdx + 13);
                if (timeEnd != -1) {
                    String timeStr = line.substring(timeIdx + 13, timeEnd);
                    try {
                        Instant currentInstant = Instant.parse(timeStr);
                        if (currentInstant.isAfter(targetInstant)) {
                            continue; // Ignore anything after 20:35
                        }
                    } catch (Exception e) {}
                }
            }

            // Check if this line contains view_file output
            if (line.contains("The following code has been modified to include a line number before every line")) {
                for (String t : targets) {
                    if (line.contains("File Path: `file:///H:/java%20spring/interviewai/src/main/resources/templates/" + t + "`") ||
                        line.contains("File Path: `file:///h:/java%20spring/interviewai/src/main/resources/templates/" + t + "`")) {
                        
                        // Extract content
                        int codeStart = line.indexOf("The following code has been modified");
                        int newlineStart = line.indexOf("\\n", codeStart);
                        if (newlineStart != -1) {
                            String rawContent = line.substring(newlineStart + 2);
                            if (rawContent.contains("The above content does NOT show")) {
                                rawContent = rawContent.substring(0, rawContent.indexOf("The above content does NOT show"));
                            } else if (rawContent.contains("The above content shows the entire")) {
                                rawContent = rawContent.substring(0, rawContent.indexOf("The above content shows the entire"));
                            }
                            
                            // Remove line numbers "1: "
                            String[] lines = rawContent.split("\\\\n");
                            StringBuilder sb = new StringBuilder();
                            for (String l : lines) {
                                int colon = l.indexOf(": ");
                                if (colon != -1 && colon < 10) {
                                    sb.append(l.substring(colon + 2)).append("\n");
                                } else {
                                    sb.append(l).append("\n");
                                }
                            }
                            latestContent.put(t, unescapeJson(sb.toString()));
                        }
                    }
                }
            }

            // Check write_to_file or replace_file_content arguments
            for (String t : targets) {
                if (line.contains("\"TargetFile\":\"H:\\\\java spring\\\\interviewai\\\\src\\\\main\\\\resources\\\\templates\\\\" + t + "\"") ||
                    line.contains("\"TargetFile\":\"h:\\\\java spring\\\\interviewai\\\\src\\\\main\\\\resources\\\\templates\\\\" + t + "\"")) {
                    
                    int codeContentIdx = line.indexOf("\"CodeContent\":\"");
                    if (codeContentIdx != -1) {
                        int endIdx = line.indexOf("\"", codeContentIdx + 15);
                        while (endIdx != -1 && line.charAt(endIdx - 1) == '\\') {
                            endIdx = line.indexOf("\"", endIdx + 1);
                        }
                        if (endIdx != -1) {
                            String content = line.substring(codeContentIdx + 15, endIdx);
                            latestContent.put(t, unescapeJson(content));
                        }
                    }
                }
            }
        }
        reader.close();

        for (String t : latestContent.keySet()) {
            Files.write(Paths.get(destDir + t), latestContent.get(t).getBytes("UTF-8"));
            System.out.println("Restored to pre-20:35 state: " + t);
        }
    }

    private static String unescapeJson(String text) {
        text = text.replace("\\\"", "\"");
        text = text.replace("\\\\", "\\");
        text = text.replace("\\r", "\r");
        text = text.replace("\\t", "\t");
        return text;
    }
}
