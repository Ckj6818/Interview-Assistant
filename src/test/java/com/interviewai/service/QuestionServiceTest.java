package com.interviewai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.entity.Question;
import com.interviewai.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    private static final String CACHE_KEY_ALL = "questions:all";

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QuestionService questionService;

    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() {
        sampleQuestions = List.of(
                new Question(1L, "Java基础", "什么是多态？", "答案", "简单", "conceptual", null),
                new Question(2L, "Java基础", "HashMap原理", "答案", "中等", "conceptual", null)
        );
    }

    @Test
    void getAllQuestions_shouldReturnFromRedis_whenCacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY_ALL)).thenReturn("cached-json");
        when(objectMapper.convertValue(eq("cached-json"), any(TypeReference.class))).thenReturn(sampleQuestions);

        List<Question> result = questionService.getAllQuestions();

        assertThat(result).hasSize(2);
        verify(questionRepository, never()).findAll();
    }

    @Test
    void getAllQuestions_shouldLoadFromMysqlAndWriteCache_whenCacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY_ALL)).thenReturn(null);
        when(questionRepository.findAll()).thenReturn(sampleQuestions);

        List<Question> result = questionService.getAllQuestions();

        assertThat(result).hasSize(2);
        verify(questionRepository).findAll();
        verify(valueOperations).set(eq(CACHE_KEY_ALL), eq(sampleQuestions), any(Duration.class));
    }

    @Test
    void getAllQuestions_shouldFallbackToMysql_whenRedisDown() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY_ALL)).thenThrow(new RuntimeException("Connection refused"));
        when(questionRepository.findAll()).thenReturn(sampleQuestions);

        List<Question> result = questionService.getAllQuestions();

        assertThat(result).hasSize(2);
        verify(questionRepository).findAll();
    }
}
