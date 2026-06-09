package com.interviewai.controller;

import com.interviewai.dto.ApiResponse;
import com.interviewai.entity.Question;
import com.interviewai.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Question API", description = "题库 REST 接口（v1）")
public class QuestionApiController {

    @Autowired
    private QuestionService questionService;

    @Operation(
            summary = "查询题库列表",
            description = "返回题库列表，支持按技术栈标签（tag）和难度（difficulty）可选筛选。数据来自 QuestionService，命中 Redis 缓存时直接返回。"
    )
    @GetMapping("/questions")
    public ApiResponse<List<Question>> listQuestions(
            @Parameter(description = "技术栈标签，对应题目 category，如 Java基础、Spring框架")
            @RequestParam(required = false) String tag,
            @Parameter(description = "难度：简单 / 中等 / 困难")
            @RequestParam(required = false) String difficulty) {
        List<Question> questions = questionService.listQuestions(tag, difficulty);
        return ApiResponse.success(questions);
    }
}
