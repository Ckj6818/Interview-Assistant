package com.mindspark.interviewai;

import java.io.*;
import java.net.*;
import java.util.*;

public class TestAuth {
    public static void main(String[] args) throws Exception {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        // 1. Login
        URL loginUrl = new URL("http://localhost:8081/login");
        HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String params = "username=test&password=password";
        conn.getOutputStream().write(params.getBytes("UTF-8"));
        conn.getResponseCode(); // Trigger request

        // 2. Fetch /records
        URL recordsUrl = new URL("http://localhost:8081/records");
        HttpURLConnection conn2 = (HttpURLConnection) recordsUrl.openConnection();
        conn2.setRequestMethod("GET");
        int code = conn2.getResponseCode();
        System.out.println("Response Code: " + code);
        
        InputStream is = (code >= 400) ? conn2.getErrorStream() : conn2.getInputStream();
        if (is != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
