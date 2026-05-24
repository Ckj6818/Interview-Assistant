package com.interviewai.controller;

import com.interviewai.entity.User;
import com.interviewai.service.InterviewRecordService;
import com.interviewai.service.QuestionService;
import com.interviewai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private InterviewRecordService interviewRecordService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            long totalQuestions = questionService.getTotalQuestionCount();
            long userInterviewCount = interviewRecordService.getInterviewCountByUserId(user.getId());

            model.addAttribute("totalQuestions", totalQuestions);
            model.addAttribute("userInterviewCount", userInterviewCount);
            model.addAttribute("username", user.getUsername());
        }
        return "index";
    }
}
