import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class FixHTML {
    public static void main(String[] args) throws Exception {
        String[] files = {"copilot.html", "mock_interview.html", "resume.html", "records.html"};
        String dir = "H:\\java spring\\interviewai\\src\\main\\resources\\templates\\";
        
        Pattern pattern = Pattern.compile("\\?+/([a-zA-Z0-9]+)>");
        
        for (String filename : files) {
            File f = new File(dir + filename);
            if (!f.exists()) continue;
            
            String content = new String(Files.readAllBytes(f.toPath()), "UTF-8");
            Matcher matcher = pattern.matcher(content);
            String repaired = matcher.replaceAll("</$1>");
            
            Files.write(f.toPath(), repaired.getBytes("UTF-8"));
            System.out.println("Repaired " + filename);
        }
    }
}
