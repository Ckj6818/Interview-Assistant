package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ExtractAllFiles {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\2296eaa3-c010-4595-87a0-370ef191fa6b\\.system_generated\\logs\\transcript.jsonl";
        String destDir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\recovered_";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // Find all TargetFile values
            int fileIdx = line.indexOf("\"TargetFile\":\"");
            if (fileIdx != -1) {
                int endFileIdx = line.indexOf("\"", fileIdx + 14);
                String filePath = line.substring(fileIdx + 14, endFileIdx).replace("\\\\", "\\");
                
                // Get filename
                String fileName = new File(filePath).getName();
                if (fileName.endsWith(".html") || fileName.endsWith(".css") || fileName.endsWith(".js") || fileName.endsWith(".yml")) {
                    int codeIdx = line.indexOf("\"CodeContent\":\"");
                    if (codeIdx != -1) {
                        int endCodeIdx = line.indexOf("\"", codeIdx + 15);
                        // handle escaped quotes
                        while (endCodeIdx != -1 && line.charAt(endCodeIdx - 1) == '\\') {
                            endCodeIdx = line.indexOf("\"", endCodeIdx + 1);
                        }
                        if (endCodeIdx != -1) {
                            String content = unescapeJson(line.substring(codeIdx + 15, endCodeIdx));
                            Files.write(Paths.get(destDir + fileName), content.getBytes("UTF-8"));
                            System.out.println("Recovered " + fileName);
                        }
                    }
                }
            }
        }
        reader.close();
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
