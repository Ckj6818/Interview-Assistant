import java.io.*;
import java.nio.charset.StandardCharsets;

public class FindBrokenChars {
    public static void main(String[] args) throws Exception {
        File f = new File("src/main/resources/templates/mock_interview.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        String line;
        int lineNum = 1;
        while ((line = reader.readLine()) != null) {
            // Check for incomplete/truncated characters (replacement char, or truncated multi-byte)
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\uFFFD' || c == '\u0000') {
                    System.out.println("Line " + lineNum + " col " + i + ": U+" + String.format("%04X", (int)c) + " => " + line.substring(Math.max(0,i-10), Math.min(line.length(), i+10)));
                }
            }
            // Also check for common broken patterns: Chinese char followed by ? at end of string literal
            if (line.contains("�")) {
                System.out.println("Line " + lineNum + " [HALF]: " + line.trim());
            }
            lineNum++;
        }
        reader.close();
        System.out.println("Scan complete. Total lines: " + (lineNum-1));
    }
}
