package com.interviewai.service;

import com.fasterxml.jackson.databind.JavaType;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Question.class);
        return getFromCacheOrLoad(CACHE_KEY_ALL, questionRepository::findAll, type);
    }

    public List<Question> getQuestionsByCategory(String category) {
        String cacheKey = CACHE_KEY_CATEGORY_PREFIX + category;
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, Question.class);
        return getFromCacheOrLoad(cacheKey, () -> questionRepository.findByCategory(category), type);
    }

    public List<String> getDistinctCategories() {
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        return getFromCacheOrLoad(CACHE_KEY_CATEGORIES, questionRepository::findDistinctCategories, type);
    }

    /**
     * 按技术栈标签与难度查询题库（供 REST API 与页面复用）。
     * tag 对应 category；difficulty 在内存中二次过滤。
     */
    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    public Question save(Question question) {
        Question saved = questionRepository.save(question);
        evictAllCaches();
        return saved;
    }

    public void deleteById(Long id) {
        questionRepository.deleteById(id);
        evictAllCaches();
    }

    public List<Question> listQuestions(String tag, String difficulty) {
        List<Question> questions = (tag != null && !tag.isBlank())
                ? getQuestionsByCategory(tag.trim())
                : getAllQuestions();

        if (difficulty == null || difficulty.isBlank()) {
            return questions;
        }

        String normalizedDifficulty = difficulty.trim();
        return questions.stream()
                .filter(q -> q.getDifficulty() != null
                        && q.getDifficulty().equalsIgnoreCase(normalizedDifficulty))
                .collect(Collectors.toList());
    }

    /**
     * 管理后台题库查询（直接查库，保证增删改后列表实时准确）。
     */
    public List<Question> searchForAdmin(String category, String keyword, String difficulty) {
        List<Question> questions = questionRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .collect(Collectors.toList());

        if (category != null && !category.isBlank()) {
            String cat = category.trim();
            questions = questions.stream()
                    .filter(q -> cat.equals(q.getCategory()))
                    .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            questions = questions.stream()
                    .filter(q -> q.getTitle() != null && q.getTitle().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        if (difficulty != null && !difficulty.isBlank()) {
            String normalizedDifficulty = difficulty.trim();
            questions = questions.stream()
                    .filter(q -> q.getDifficulty() != null
                            && q.getDifficulty().equalsIgnoreCase(normalizedDifficulty))
                    .collect(Collectors.toList());
        }

        return questions;
    }

    private <T> T getFromCacheOrLoad(String cacheKey, Supplier<T> loader, JavaType javaType) {
        T cached = readFromRedis(cacheKey, javaType);
        if (cached != null) {
            log.info("[Redis HIT] key={}", cacheKey);
            return cached;
        }

        log.info("[Redis MISS] key={}, loading from MySQL", cacheKey);
        T data = loader.get();
        writeToRedis(cacheKey, data);
        return data;
    }

    @SuppressWarnings("unchecked")
    private <T> T readFromRedis(String cacheKey, JavaType javaType) {
        if (redisTemplate == null) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return null;
            }
            return (T) objectMapper.convertValue(value, javaType);
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

    private void evictAllCaches() {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(CACHE_KEY_ALL);
            redisTemplate.delete(CACHE_KEY_CATEGORIES);
            Set<String> categoryKeys = redisTemplate.keys(CACHE_KEY_CATEGORY_PREFIX + "*");
            if (categoryKeys != null && !categoryKeys.isEmpty()) {
                redisTemplate.delete(categoryKeys);
            }
            log.info("[Redis EVICT] question caches cleared");
        } catch (Exception e) {
            log.warn("[Redis DOWN] cache eviction failed: {}", e.getMessage());
        }
    }
}
