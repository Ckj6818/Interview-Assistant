package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RestoreHistory2 {
    public static void main(String[] args) throws Exception {
        String historyDir = "C:\\Users\\asus\\AppData\\Roaming\\Code\\User\\History";
        Map<String, List<File>> matches = new HashMap<>();
        matches.put("copilot", new ArrayList<>());
        matches.put("mock_interview", new ArrayList<>());
        matches.put("resume", new ArrayList<>());
        matches.put("records", new ArrayList<>());

        Files.walk(Paths.get(historyDir))
             .filter(Files::isRegularFile)
             .forEach(path -> {
                 try {
                     File f = path.toFile();
                     String content = new String(Files.readAllBytes(path), "UTF-8");
                     if (content.contains("MindSpark")) {
                         if (content.contains("面试设定")) matches.get("copilot").add(f);
                         if (content.contains("全真模拟面试")) matches.get("mock_interview").add(f);
                         if (content.contains("简历")) matches.get("resume").add(f);
                         if (content.contains("面试记录")) matches.get("records").add(f);
                     }
                 } catch (Exception e) {}
             });

        for (String key : matches.keySet()) {
            System.out.println("Matches for " + key + ":");
            matches.get(key).sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            for (int i = 0; i < Math.min(5, matches.get(key).size()); i++) {
                File f = matches.get(key).get(i);
                System.out.println("  " + f.getAbsolutePath() + " (" + new Date(f.lastModified()) + ")");
            }
        }
    }
}
