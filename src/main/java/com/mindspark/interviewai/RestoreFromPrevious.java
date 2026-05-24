package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RestoreFromPrevious {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\2296eaa3-c010-4595-87a0-370ef191fa6b\\.system_generated\\logs\\transcript.jsonl";
        String[] targets = {"copilot.html", "mock_interview.html", "resume.html", "records.html", "questions.html", "index.html", "login.html", "register.html", "interview.html", "report.html", "leetcode.html", "application.yml"};
        String destDir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";

        // Read the file and find the last write_to_file or replace_file_content for each target
        Map<String, String> fileContents = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String t : targets) {
                    if (line.contains("\"TargetFile\":\"H:\\\\java spring\\\\interviewai\\\\src\\\\main\\\\resources\\\\templates\\\\" + t + "\"") ||
                        line.contains("\"TargetFile\":\"h:\\\\java spring\\\\interviewai\\\\src\\\\main\\\\resources\\\\templates\\\\" + t + "\"") ||
                        line.contains("\"TargetFile\":\"H:\\\\java spring\\\\interviewai\\\\src\\\\main\\\\resources\\\\application.yml\"")) {
                        
                        // Try to extract CodeContent or ReplacementContent using simple string manipulation
                        int codeContentIdx = line.indexOf("\"CodeContent\":\"");
                        if (codeContentIdx != -1) {
                            int endIdx = line.indexOf("\"", codeContentIdx + 15);
                            while (endIdx != -1 && line.charAt(endIdx - 1) == '\\') {
                                endIdx = line.indexOf("\"", endIdx + 1);
                            }
                            if (endIdx != -1) {
                                String content = line.substring(codeContentIdx + 15, endIdx);
                                fileContents.put(t, unescapeJson(content));
                            }
                        }
                    }
                }
            }
        }

        for (String t : targets) {
            if (fileContents.containsKey(t)) {
                String outPath = t.equals("application.yml") ? "H:\\java spring\\interviewai\\src\\main\\resources\\application.yml" : destDir + t;
                Files.write(Paths.get(outPath), fileContents.get(t).getBytes("UTF-8"));
                System.out.println("Restored: " + t);
            } else {
                System.out.println("Not found: " + t);
            }
        }
    }

    private static String unescapeJson(String text) {
        text = text.replace("\\n", "\n");
        text = text.replace("\\\"", "\"");
        text = text.replace("\\\\", "\\");
        text = text.replace("\\r", "\r");
        text = text.replace("\\t", "\t");
        return text;
    }
}
