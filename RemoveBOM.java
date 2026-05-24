import java.io.*;
import java.nio.file.*;

public class RemoveBOM {
    public static void main(String[] args) throws Exception {
        String[] dirs = {
            "H:\\java spring\\interviewai\\src\\main\\resources\\templates",
            "H:\\java spring\\interviewai\\target\\classes\\templates"
        };
        for (String d : dirs) {
            File dir = new File(d);
            if (!dir.exists()) continue;
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".html")) {
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
                        byte[] newBytes = new byte[bytes.length - 3];
                        System.arraycopy(bytes, 3, newBytes, 0, newBytes.length);
                        Files.write(f.toPath(), newBytes);
                        System.out.println("Removed BOM from " + f.getAbsolutePath());
                    }
                }
            }
        }
    }
}
