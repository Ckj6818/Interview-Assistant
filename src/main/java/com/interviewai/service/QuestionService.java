package com.interviewai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.entity.Question;
import com.interviewai.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final String CACHE_KEY_ALL = "questions:all";
    private static final String CACHE_KEY_CATEGORY_PREFIX = "questions:category:";
    private static final String CACHE_KEY_CATEGORIES = "questions:categories";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public long getTotalQuestionCount() {
        return questionRepository.count();
    }

    public List<Question> getAllQuestions() {
        return getFromCacheOrLoad(CACHE_KEY_ALL, questionRepository::findAll, new TypeReference<>() {});
    }

    public List<Question> getQuestionsByCategory(String category) {
        String cacheKey = CACHE_KEY_CATEGORY_PREFIX + category;
        return getFromCacheOrLoad(cacheKey, () -> questionRepository.findByCategory(category), new TypeReference<>() {});
    }

    public List<String> getDistinctCategories() {
        return getFromCacheOrLoad(CACHE_KEY_CATEGORIES, questionRepository::findDistinctCategories, new TypeReference<>() {});
    }

    private <T> T getFromCacheOrLoad(String cacheKey, Supplier<T> loader, TypeReference<T> typeRef) {
        T cached = readFromRedis(cacheKey, typeRef);
        if (cached != null) {
            log.info("[Redis HIT] key={}", cacheKey);
            return cached;
        }

        log.info("[Redis MISS] key={}, loading from MySQL", cacheKey);
        T data = loader.get();
        writeToRedis(cacheKey, data);
        return data;
    }

    private <T> T readFromRedis(String cacheKey, TypeReference<T> typeRef) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return null;
            }
            return objectMapper.convertValue(value, typeRef);
        } catch (Exception e) {
            log.warn("[Redis DOWN] read failed for key={}, fallback to MySQL: {}", cacheKey, e.getMessage());
            return null;
        }
    }

    private void writeToRedis(String cacheKey, Object data) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(cacheKey, data, CACHE_TTL);
            log.info("[Redis SET] key={}, ttl={}min", cacheKey, CACHE_TTL.toMinutes());
        } catch (Exception e) {
            log.warn("[Redis DOWN] write failed for key={}, continuing without cache: {}", cacheKey, e.getMessage());
        }
    }
}
