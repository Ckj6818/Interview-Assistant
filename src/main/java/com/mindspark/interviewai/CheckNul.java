package com.mindspark.interviewai;

import java.io.*;
import java.nio.file.*;

public class CheckNul {
    public static void main(String[] args) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("H:\\java spring\\interviewai\\src\\main\\resources\\templates\\mock_interview.html"));
        int nulCount = 0;
        for (byte b : bytes) {
            if (b == 0) nulCount++;
        }
        System.out.println("NUL bytes in mock_interview.html: " + nulCount);

        bytes = Files.readAllBytes(Paths.get("H:\\java spring\\interviewai\\src\\main\\resources\\templates\\records.html"));
        nulCount = 0;
        for (byte b : bytes) {
            if (b == 0) nulCount++;
        }
        System.out.println("NUL bytes in records.html: " + nulCount);
    }
}
