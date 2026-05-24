package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ExtractOldFiles {
    public static void main(String[] args) throws Exception {
        String logPath1 = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\2296eaa3-c010-4595-87a0-370ef191fa6b\\.system_generated\\logs\\transcript.jsonl";
        String logPath2 = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        
        String[] targets = {"copilot.html", "mock_interview.html", "resume.html", "records.html", "questions.html", "index.html", "login.html", "register.html", "interview.html", "report.html", "leetcode.html"};
        String destDir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";

        // Try reading from logs to find the first valid block
        processLog(logPath1, targets, destDir);
        processLog(logPath2, targets, destDir);
    }

    private static void processLog(String logPath, String[] targets, String destDir) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                // If the log contains the original files, it's probably in a "view_file" or "write_to_file"
                // We'll just print out when we find them for now
                if (line.contains("The following code has been modified to include a line number before every line")) {
                    for (String t : targets) {
                        if (line.contains("File Path: `file:///H:/java%20spring/interviewai/src/main/resources/templates/" + t + "`")) {
                            System.out.println("Found " + t + " in " + logPath + "!");
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {}
    }
}
