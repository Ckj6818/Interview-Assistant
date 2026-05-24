import java.io.*;

public class CheckFFFD {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/templates");
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(".html")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                String line;
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\uFFFD")) {
                        System.out.println(f.getName() + ":" + lineNum + " contains U+FFFD");
                    }
                    lineNum++;
                }
                reader.close();
            }
        }
    }
}
