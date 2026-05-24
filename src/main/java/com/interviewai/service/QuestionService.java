package com.interviewai.service;

import com.interviewai.entity.Question;
import com.interviewai.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public long getTotalQuestionCount() {
        return questionRepository.count();
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
}
