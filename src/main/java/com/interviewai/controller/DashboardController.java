package com.interviewai.controller;

// We will just redirect to home or handle similarly if DashboardController exists separately.
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/";
    }
}
