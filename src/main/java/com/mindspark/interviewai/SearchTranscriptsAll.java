package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SearchTranscriptsAll {
    public static void main(String[] args) throws Exception {
        String brainDir = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain";
        Path brainPath = Paths.get(brainDir);
        
        List<Path> transcripts = new ArrayList<>();
        Files.walkFileTree(brainPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals("transcript.jsonl")) {
                    transcripts.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        String bestContent = null;
        int maxLen = 0;
        
        for (Path tPath : transcripts) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tPath.toFile()), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("copilot.html") && line.contains("CodeContent")) {
                        int codeStart = line.indexOf("\"CodeContent\":\"");
                        if (codeStart != -1) {
                            int endIdx = line.indexOf("\"", codeStart + 15);
                            while (endIdx != -1 && line.charAt(endIdx - 1) == '\\') {
                                endIdx = line.indexOf("\"", endIdx + 1);
                            }
                            if (endIdx != -1) {
                                String content = unescapeJson(line.substring(codeStart + 15, endIdx));
                                if (!content.contains("\ufffd") && content.length() > maxLen) {
                                    bestContent = content;
                                    maxLen = content.length();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (bestContent != null) {
            Files.write(Paths.get("src/main/resources/templates/copilot.html"), bestContent.getBytes("UTF-8"));
            System.out.println("Recovered copilot.html! Length: " + maxLen);
        } else {
            System.out.println("Could not find uncorrupted copilot.html");
        }
    }
    
    private static String unescapeJson(String text) {
        text = text.replace("\\\"", "\"");
        text = text.replace("\\\\", "\\");
        text = text.replace("\\r", "\r");
        text = text.replace("\\n", "\n");
        text = text.replace("\\t", "\t");
        return text;
    }
}
