package com.interviewai;

import com.interviewai.util.ResumeParser;
import org.springframework.mock.web.MockMultipartFile;
import java.io.File;
import java.nio.file.Files;

public class TestPdf {
    public static void main(String[] args) {
        try {
            System.out.println("Trying to load ResumeParser class...");
            Class.forName("com.interviewai.util.ResumeParser");
            System.out.println("ResumeParser loaded successfully!");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
