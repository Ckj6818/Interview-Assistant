package com.mindspark.interviewai;

import java.io.*;
import java.net.*;
import java.util.*;

public class TestMockInterview {
    public static void main(String[] args) throws Exception {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        // 1. Get Login Page to get CSRF token
        URL loginPageUrl = new URL("http://localhost:8082/login");
        HttpURLConnection connPage = (HttpURLConnection) loginPageUrl.openConnection();
        connPage.setRequestMethod("GET");
        connPage.getResponseCode();
        
        String csrfToken = "";
        BufferedReader brPage = new BufferedReader(new InputStreamReader(connPage.getInputStream(), "UTF-8"));
        String linePage;
        while ((linePage = brPage.readLine()) != null) {
            if (linePage.contains("name=\"_csrf\"")) {
                int valIdx = linePage.indexOf("value=\"");
                if (valIdx != -1) {
                    csrfToken = linePage.substring(valIdx + 7, linePage.indexOf("\"", valIdx + 7));
                    break;
                }
            }
        }
        System.out.println("CSRF: " + csrfToken);

        // 2. Login POST
        URL loginUrl = new URL("http://localhost:8082/login");
        HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String params = "username=test&password=password&_csrf=" + csrfToken;
        conn.getOutputStream().write(params.getBytes("UTF-8"));
        int loginCode = conn.getResponseCode();
        System.out.println("Login Code: " + loginCode);
        
        // Read login response to clear buffer
        InputStream isLogin = (loginCode >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (isLogin != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(isLogin, "UTF-8"));
            while (br.readLine() != null) {}
        }

        // 3. Fetch /mock-interview
        URL mockUrl = new URL("http://localhost:8082/mock-interview");
        HttpURLConnection conn2 = (HttpURLConnection) mockUrl.openConnection();
        conn2.setRequestMethod("GET");
        int code = conn2.getResponseCode();
        System.out.println("Mock Code: " + code);
        
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
