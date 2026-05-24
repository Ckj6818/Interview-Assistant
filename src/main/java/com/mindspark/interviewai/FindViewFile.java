package com.mindspark.interviewai;

import java.io.*;

public class FindViewFile {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("VIEW_FILE") || line.contains("\"name\":\"view_file\"")) {
                if (line.contains("resume.html") || line.contains("copilot.html") || line.contains("mock_interview.html") || line.contains("records.html")) {
                    // print the timestamp or step index
                    int typeIdx = line.indexOf("\"type\"");
                    int timeIdx = line.indexOf("\"timestamp\"");
                    System.out.println("View_file found at: " + (timeIdx != -1 ? line.substring(timeIdx, timeIdx + 40) : line.substring(0, 100)));
                }
            }
        }
        reader.close();
    }
}
