package com.interviewai.controller;

import com.interviewai.entity.Question;
import com.interviewai.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/questions-list")
@Tag(name = "Question", description = "题库列表查询（Redis 缓存）")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Operation(summary = "题库列表页", description = "返回全部题目列表页面，数据来自 Redis 缓存")
    @GetMapping
    public String listQuestions(Model model) {
        List<Question> questions = questionService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "questions";
    }
}
