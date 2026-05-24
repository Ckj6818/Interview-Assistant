package com.interviewai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "interview_records")
public class InterviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    // 综合评分
    private Integer score;

    // 雷达图细分维度评分 (0-100) - 六维六边形雷达
    private Integer foundationScore; // 基础知识
    private Integer logicScore;      // 逻辑思维
    private Integer communicationScore; // 沟通表达
    private Integer stressScore;     // 抗压应变
    private Integer techScore;       // 技术深度
    private Integer businessScore;   // 业务场景

    // 针对回答的 STAR 法则分析记录 (存储为 JSON 字符串)
    @Column(columnDefinition = "TEXT")
    private String starAnalysisJson;

    // AI生成的长文本评测报告
    @Column(columnDefinition = "TEXT")
    private String feedbackReport;

    // 改进建议与参考答案
    @Column(columnDefinition = "TEXT")
    private String suggestions;

    // 官方标准答案
    @Column(columnDefinition = "TEXT")
    private String standardAnswer;

    // 全真模拟面试的完整多轮对话记录
    @Column(columnDefinition = "LONGTEXT")
    private String chatLog;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
