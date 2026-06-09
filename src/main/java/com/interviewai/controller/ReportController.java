package com.interviewai.controller;

import com.interviewai.entity.InterviewRecord;
import com.interviewai.entity.User;
import com.interviewai.service.InterviewRecordService;
import com.interviewai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Tag(name = "Report", description = "面试报告与历史记录")
public class ReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private InterviewRecordService interviewRecordService;

    @Operation(summary = "我的报告列表", description = "查看当前用户的全部面试记录与报告")
    @GetMapping("/my-reports")
    public String myReports(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();
        List<InterviewRecord> records = interviewRecordService.getRecordsByUserId(user.getId());
        
        model.addAttribute("records", records);
        model.addAttribute("username", username);
        return "records"; // Use the records template
    }
}
