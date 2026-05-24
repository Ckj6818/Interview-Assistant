package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.*;
import java.util.*;

public class RestoreHistory {
    public static void main(String[] args) throws Exception {
        String historyDir = "C:\\Users\\asus\\AppData\\Roaming\\Code\\User\\History";
        String[] targets = {"copilot.html", "mock_interview.html", "resume.html", "records.html"};
        String destDir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";

        // We want the file closest to 2026-05-24 20:32
        // Time in millis for 20:32
        LocalDateTime targetTime = LocalDateTime.of(2026, 5, 24, 20, 32);
        long targetMillis = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Files.walk(Paths.get(historyDir))
             .filter(Files::isRegularFile)
             .forEach(path -> {
                 try {
                     File f = path.toFile();
                     long modified = f.lastModified();
                     // Filter between 20:00 and 21:15
                     if (modified > targetMillis - 3600000 && modified < targetMillis + 3600000) {
                         String content = new String(Files.readAllBytes(path), "UTF-8");
                         for (String t : targets) {
                             if (content.contains("<title>") && content.contains("MindSpark") && (
                                 (t.equals("copilot.html") && content.contains("面试设定")) ||
                                 (t.equals("mock_interview.html") && content.contains("全真模拟面试")) ||
                                 (t.equals("resume.html") && content.contains("简历")) ||
                                 (t.equals("records.html") && content.contains("面试记录"))
                             )) {
                                 // Basic heuristic: check if the file matches the target type
                                 // Let's just output matches for now
                                 System.out.println("Found match for " + t + ": " + path + " modified at " + new Date(modified));
                             }
                         }
                     }
                 } catch (Exception e) {}
             });
    }
}
