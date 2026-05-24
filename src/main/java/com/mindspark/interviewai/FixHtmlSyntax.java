package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;

public class FixHtmlSyntax {
    public static void main(String[] args) throws Exception {
        String dir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";
        String[] files = {"records.html", "mock_interview.html", "resume.html", "copilot.html", "questions.html", "index.html", "login.html", "register.html", "report.html", "interview.html", "leetcode.html"};
        
        for (String file : files) {
            Path path = Paths.get(dir + file);
            if (!Files.exists(path)) continue;
            byte[] bytes = Files.readAllBytes(path);
            
            // Remove BOM if present (EF BB BF)
            int startIdx = 0;
            if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                startIdx = 3;
            }
            
            String content = new String(bytes, startIdx, bytes.length - startIdx, "UTF-8");
            
            // Fix records.html missing quote
            if (file.equals("records.html")) {
                content = content.replace("th:text=\"${record.score} + ' 分\">", "th:text=\"${record.score} + ' 分'\">");
            }

            // Fix any "【实时收音中... 请直接说话回答】" unclosed quote in mock_interview
            if (file.equals("mock_interview.html")) {
                // Let's just make sure we don't have broken JS strings
                // In UpgradeVoice, I did:
                // content = content.replace("previewText.innerText = \"【您可以点击麦克风进行回答，或点击右下角结束面试?;", "previewText.innerText = \"【实时收音中... 请直接说话回答】\";\n            startSpeechInput();");
                // The replacement string HAS the closing quote.
                // Let's check if there are any other obvious broken strings like `"` without matching
            }
            
            Files.write(path, content.getBytes("UTF-8"));
            System.out.println("Fixed BOM and syntax for " + file);
        }
    }
}
