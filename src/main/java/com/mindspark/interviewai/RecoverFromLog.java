package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecoverFromLog {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        Map<Integer, String> lines = new TreeMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("VIEW_FILE") && line.contains("copilot.html") && line.contains("Showing lines")) {
                    int contentStart = line.indexOf("\"content\":\"");
                    if (contentStart != -1) {
                        int endIdx = line.lastIndexOf("\"}");
                        if (endIdx != -1) {
                            String content = line.substring(contentStart + 11, endIdx);
                            content = content.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                            String[] parts = content.split("\n");
                            for (String p : parts) {
                                if (p.contains(":") && p.matches("^\\d+:.*")) {
                                    int colon = p.indexOf(":");
                                    int num = Integer.parseInt(p.substring(0, colon));
                                    String text = p.substring(colon + 1);
                                    if (text.startsWith(" ")) text = text.substring(1);
                                    lines.put(num, text);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!lines.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int max = Collections.max(lines.keySet());
            for (int i = 1; i <= max; i++) {
                sb.append(lines.getOrDefault(i, "")).append("\n");
            }
            Files.write(Paths.get("src/main/resources/templates/copilot.html"), sb.toString().getBytes("UTF-8"));
            System.out.println("Recovered " + lines.size() + " lines into copilot.html");
        } else {
            System.out.println("Failed to recover lines");
        }
    }
}
