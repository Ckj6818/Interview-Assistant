import java.io.*;
import java.util.*;

public class SearchTranscript {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("class=\\\"nav-link\\\" href=\\\"/copilot\\\"") || line.contains("class=\"nav-link\" href=\"/copilot\"")) {
                int stepIndex = -1;
                int typeIdx = line.indexOf("\"type\":\"");
                if (typeIdx > 0) {
                    System.out.println("Found match!");
                }
            }
        }
        reader.close();
    }
}
