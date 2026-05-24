package com.interviewai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, unique = true, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = true)
    private String difficulty; // 简单, 中等, 困难

    @Column(nullable = true)
    private String questionType; // code, conceptual

    @Column(nullable = true, columnDefinition = "TEXT")
    private String defaultCode; // 默认代码模板
}
