import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TestRecovery {
    public static void main(String[] args) throws Exception {
        String logPath = "C:\\Users\\asus\\.gemini\\antigravity-ide\\brain\\e019ca05-a167-4b4a-b0df-2623390a3909\\.system_generated\\logs\\transcript.jsonl";
        String[] targets = {"copilot.html", "mock_interview.html", "resume.html", "records.html"};
        
        System.out.println("Reading log...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), "UTF-8"));
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.contains("TOOL_RESPONSE") && line.contains("DONE")) {
                for (String t : targets) {
                    if (line.contains(t) && line.contains("The above content shows the entire, complete file contents")) {
                        // Extract content
                        int startIndex = line.indexOf("The following code has been modified to include a line number before every line");
                        if (startIndex != -1) {
                            int end = line.indexOf("The above content shows the entire, complete file contents", startIndex);
                            if (end != -1) {
                                String block = line.substring(startIndex, end);
                                String[] lines = block.split("\\\\n");
                                StringBuilder sb = new StringBuilder();
                                for (String l : lines) {
                                    if (l.contains(": ")) {
                                        sb.append(l.substring(l.indexOf(": ") + 2)).append("\n");
                                    }
                                }
                                Files.write(Paths.get("H:\\java spring\\interviewai\\src\\main\\resources\\templates\\" + t), sb.toString().getBytes("UTF-8"));
                                System.out.println("Recovered " + t);
                            }
                        }
                    }
                }
            }
        }
        reader.close();
    }
}
