package com.interviewai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.entity.InterviewRecord;
import com.interviewai.entity.InterviewerPersona;
import com.interviewai.entity.Question;
import com.interviewai.entity.User;
import com.interviewai.repository.InterviewRecordRepository;
import com.interviewai.repository.InterviewerPersonaRepository;
import com.interviewai.repository.QuestionRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.AsyncReportService;
import com.interviewai.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 面试与题库控制器
 */
@Controller
public class InterviewController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private InterviewRecordRepository interviewRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewerPersonaRepository personaRepository;

    @Autowired
    private LlmService llmService;

    @Autowired
    private AsyncReportService asyncReportService;

    /**
     * 题库大厅页面
     */
    @GetMapping("/questions")
    public String questionsPage(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "difficulty", required = false) String difficulty,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "search", required = false) String search,
            Model model,
            Authentication authentication) {
        
        List<String> categories = questionRepository.findDistinctCategories();
        List<Question> allQuestions = questionRepository.findAll();
        
        // 获取当前用户已解答的题目
        Set<String> solvedTopics = new HashSet<>();
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                List<InterviewRecord> records = interviewRecordRepository.findByUserIdOrderByCreateTimeDesc(userOpt.get().getId());
                for (InterviewRecord rec : records) {
                    solvedTopics.add(rec.getTopic());
                }
            }
        }
        
        // 分维度的 Stream 过滤逻辑
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(q -> {
                    if (category != null && !category.isEmpty()) {
                        return q.getCategory().equalsIgnoreCase(category);
                    }
                    return true;
                })
                .filter(q -> {
                    if (difficulty != null && !difficulty.isEmpty()) {
                        return q.getDifficulty() != null && q.getDifficulty().equalsIgnoreCase(difficulty);
                    }
                    return true;
                })
                .filter(q -> {
                    if (type != null && !type.isEmpty()) {
                        return q.getQuestionType() != null && q.getQuestionType().equalsIgnoreCase(type);
                    }
                    return true;
                })
                .filter(q -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.trim().toLowerCase();
                        return q.getTitle().toLowerCase().contains(s) || q.getCategory().toLowerCase().contains(s);
                    }
                    return true;
                })
                .filter(q -> {
                    if (status != null && !status.isEmpty()) {
                        boolean isSolved = solvedTopics.contains(q.getTitle());
                        if ("solved".equalsIgnoreCase(status)) {
                            return isSolved;
                        } else if ("unsolved".equalsIgnoreCase(status)) {
                            return !isSolved;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("allQuestionsJson", mapper.writeValueAsString(allQuestions));
            model.addAttribute("categoriesJson", mapper.writeValueAsString(categories));
            model.addAttribute("solvedTopicsJson", mapper.writeValueAsString(solvedTopics));
        } catch (Exception e) {
            model.addAttribute("allQuestionsJson", "[]");
            model.addAttribute("categoriesJson", "[]");
            model.addAttribute("solvedTopicsJson", "[]");
        }

        model.addAttribute("categories", categories);
        model.addAttribute("questions", filteredQuestions);
        model.addAttribute("allQuestions", allQuestions);
        model.addAttribute("solvedTopics", solvedTopics);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentDifficulty", difficulty);
        model.addAttribute("currentType", type);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);
        
        return "questions";
    }

    /**
     * AI 模拟面试室页面
     */
    @GetMapping("/interview/{questionId}")
    public String interviewRoom(@PathVariable("questionId") Long questionId, Model model) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID: " + questionId));
        
        List<InterviewerPersona> personas = personaRepository.findAll();
        
        model.addAttribute("question", question);
        model.addAttribute("personas", personas);
        return "interview";
    }

    /**
     * LeetCode-style 在线作答室页面
     */
    @GetMapping("/questions/{questionId}/solve")
    public String leetcodeRoom(@PathVariable("questionId") Long questionId, Model model, Authentication authentication) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID: " + questionId));
        
        boolean isSolved = false;
        if (authentication != null && authentication.isAuthenticated()) {
            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                isSolved = interviewRecordRepository.findByUserIdOrderByCreateTimeDesc(userOpt.get().getId())
                        .stream().anyMatch(rec -> rec.getTopic().equals(question.getTitle()));
            }
        }
        
        model.addAttribute("question", question);
        model.addAttribute("isSolved", isSolved);
        return "leetcode";
    }

    /**
     * 运行代码测试
     */
    @PostMapping("/api/questions/run")
    @ResponseBody
    public ResponseEntity<?> runCode(@RequestBody Map<String, Object> payload) {
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String code = payload.get("code").toString();
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID"));
        
        String runResultJson = llmService.runCodeCheck(question.getTitle(), question.getQuestionType(), code);
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(runResultJson);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "compileOk", false,
                    "compilerMessage", "结果解析错误: " + e.getMessage(),
                    "diagnostic", "无法解析评测系统的响应"
            ));
        }
    }

    /**
     * 实时在线算法协作：根据停顿检测返回点拨
     */
    @PostMapping("/api/questions/code-hint")
    @ResponseBody
    public ResponseEntity<?> getCodeHint(@RequestBody Map<String, Object> payload) {
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String code = payload.get("code").toString();
        
        Question question = questionRepository.findById(questionId).orElse(null);
        if (question == null) {
            return ResponseEntity.badRequest().body(Map.of("hint", "未找到题目信息"));
        }
        
        String hintJson = llmService.generateCodeHint(question.getTitle(), question.getQuestionType(), code);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(hintJson);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("hint", "【点拨异常】" + e.getMessage()));
        }
    }

    /**
     * 提交代码评测并保存记录
     */
    @PostMapping("/api/questions/submit")
    @ResponseBody
    public ResponseEntity<?> submitCode(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        String code = payload.get("code").toString();
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID"));
        
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("用户未找到"));
        
        // 检查提交解答是否为空或过短
        boolean isEmpty = (code == null || code.trim().isEmpty() || code.trim().length() < 10);
        String submitResultJson;
        if (isEmpty) {
            submitResultJson = "{\n" +
                    "  \"score\": 0,\n" +
                    "  \"foundationScore\": 0,\n" +
                    "  \"logicScore\": 0,\n" +
                    "  \"communicationScore\": 0,\n" +
                    "  \"stressScore\": 0,\n" +
                    "  \"feedbackReport\": \"【评估报告】您提交的解答内容为空或过短，未检测到实质性的作答，因此评估分为 0。\",\n" +
                    "  \"suggestions\": \"【建议】请在编辑器中编写完整的代码或技术设计概念后再点击提交。\"\n" +
                    "}";
        } else {
            // 调用大模型进行打分和评估
            submitResultJson = llmService.submitQuestionCheck(
                    question.getTitle(),
                    question.getQuestionType(),
                    code,
                    question.getAnswer()
            );
        }
        
        InterviewRecord record = new InterviewRecord();
        record.setUser(user);
        record.setTopic(question.getTitle());
        record.setStandardAnswer(question.getAnswer());
        record.setChatLog(code); // 将提交的代码或文本保存在 chatLog 字段
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(submitResultJson);
            
            record.setScore(root.has("score") ? root.get("score").asInt() : 0);
            record.setFoundationScore(root.has("foundationScore") ? root.get("foundationScore").asInt() : 0);
            record.setLogicScore(root.has("logicScore") ? root.get("logicScore").asInt() : 0);
            record.setCommunicationScore(root.has("communicationScore") ? root.get("communicationScore").asInt() : 0);
            record.setStressScore(root.has("stressScore") ? root.get("stressScore").asInt() : 0);
            record.setFeedbackReport(root.has("feedbackReport") ? root.get("feedbackReport").asText() : "暂无评价");
            record.setSuggestions(root.has("suggestions") ? root.get("suggestions").asText() : "");
        } catch (Exception e) {
            record.setScore(0);
            record.setFeedbackReport(submitResultJson);
        }
        
        interviewRecordRepository.save(record);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "recordId", record.getId()
        ));
    }

    /**
     * 获取单条面试/刷题记录详情的 API 接口
     */
    @GetMapping("/api/records-detail/{recordId}")
    @ResponseBody
    public ResponseEntity<?> getRecordDetail(@PathVariable("recordId") Long recordId, Authentication authentication) {
        InterviewRecord record = interviewRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("无效的记录ID"));
        
        if (!record.getUser().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(403).body("无权查看此报告");
        }
        
        // 构造扁平 Map 避免懒加载或循环引用问题
        Map<String, Object> response = Map.of(
                "score", record.getScore() != null ? record.getScore() : 60,
                "foundationScore", record.getFoundationScore() != null ? record.getFoundationScore() : 60,
                "logicScore", record.getLogicScore() != null ? record.getLogicScore() : 60,
                "communicationScore", record.getCommunicationScore() != null ? record.getCommunicationScore() : 60,
                "stressScore", record.getStressScore() != null ? record.getStressScore() : 60,
                "feedbackReport", record.getFeedbackReport() != null ? record.getFeedbackReport() : "暂无评价",
                "suggestions", record.getSuggestions() != null ? record.getSuggestions() : ""
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 接收前端 Ajax 提交的单轮聊天请求 (SSE 流式打字机)
     */
    @PostMapping(value = "/api/interview/chat-stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<String> chatRoundStream(@RequestBody Map<String, Object> payload) {
        List<Map<String, String>> chatHistory = (List<Map<String, String>>) payload.get("chatHistory");
        Long personaId = null;
        if (payload.get("personaId") != null && !payload.get("personaId").toString().isEmpty()) {
            personaId = Long.valueOf(payload.get("personaId").toString());
        }
        
        String personaPrompt = "";
        if (personaId != null) {
            personaPrompt = personaRepository.findById(personaId)
                    .map(InterviewerPersona::getPromptTemplate)
                    .orElse("");
        }

        // 注入系统 prompt
        List<Map<String, String>> finalHistory = new java.util.ArrayList<>();
        String systemMsg = "你是一位技术面试官。" + personaPrompt + "\n请根据候选人的回答进行追问或评价。每次只问一个核心问题，不要长篇大论，语气像真实的对话。";
        finalHistory.add(Map.of("role", "system", "content", systemMsg));
        finalHistory.addAll(chatHistory);

        return llmService.chatInterviewRoundStream(finalHistory);
    }

    /**
     * 接收前端 Ajax 提交的完整面试记录，进行全场结算评分，并保存记录
     */
    @PostMapping("/api/interview/submit")
    @ResponseBody
    public ResponseEntity<?> submitSession(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        Long questionId = Long.valueOf(payload.get("questionId"));
        String chatHistoryJson = payload.get("chatHistoryJson");
        Long personaId = payload.get("personaId") != null && !payload.get("personaId").isEmpty() ? Long.valueOf(payload.get("personaId")) : null;
        
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID"));

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        String personaPrompt = "";
        if (personaId != null) {
            personaPrompt = personaRepository.findById(personaId)
                    .map(InterviewerPersona::getPromptTemplate)
                    .orElse("");
        }

        // 先保存一个初始状态的记录，立即返回给前端，不阻塞响应
        InterviewRecord record = new InterviewRecord();
        record.setUser(user);
        record.setTopic(question.getTitle());
        record.setStandardAnswer(question.getAnswer());
        record.setChatLog(chatHistoryJson); // 保存到数据库
        record.setScore(0);
        record.setFeedbackReport("AI 正在对您的全场表现进行深度评估，可能需要几十秒的时间，请稍候刷新页面查看最终报告...");
        
        record = interviewRecordRepository.save(record);

        // 异步调用大模型进行报告生成
        asyncReportService.generateAsyncReport(record.getId(), question.getTitle(), question.getAnswer(), personaPrompt, chatHistoryJson);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "recordId", record.getId()
        ));
    }

    /**
     * 个人面试记录列表
     */
    @GetMapping("/records")
    public String recordsPage(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        List<InterviewRecord> records = interviewRecordRepository.findByUserIdOrderByCreateTimeDesc(user.getId());
        model.addAttribute("records", records);
        return "records";
    }

    /**
     * 单份面试报告详细页
     */
    @GetMapping("/report/{recordId}")
    public String reportPage(@PathVariable("recordId") Long recordId, Model model, Authentication authentication) {
        InterviewRecord record = interviewRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("无效的记录ID"));
        
        if (!record.getUser().getUsername().equals(authentication.getName())) {
            throw new RuntimeException("无权查看此报告");
        }

        model.addAttribute("record", record);
        return "report";
    }

    /**
     * AI 笔试秒解分析接口 (由 AI 模拟面试板块调用)
     */
    @PostMapping("/api/solver/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        
        // 模拟 AI 视觉模型处理耗时
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        // 根据所选题型返回模拟的“高分智能解答” (对应参考截图中的数据)
        if ("code".equals(type)) {
            result.put("correctAnswer", "def is_anagram(s1, s2):\n    return sorted(s1) == sorted(s2)");
            result.put("reasoning", "将两个字符串排序后比较是否相等，如果相等则说明它们由相同字符组成。\n<br><span style='color: #64748b;'>● 时间复杂度: O(n log n)，空间复杂度: O(n)</span>");
        } else if ("choice".equals(type)) {
            result.put("correctAnswer", "C. 栈 (Stack)");
            result.put("reasoning", "栈的插入操作是将元素压入栈顶，不涉及移动其他元素，平均时间复杂度为 O(1)。");
        } else if ("image".equals(type)) {
            result.put("correctAnswer", "3月用户活跃数最高");
            result.put("reasoning", "3月用户活跃数为12万，是四个月中最高的数值。");
        } else if ("logic".equals(type)) {
            result.put("correctAnswer", "A. 小李不参与核心模块开发");
            result.put("reasoning", "根据条件①，参与核心模块开发的员工必须签署保密协议；小李没有签署，因此可推出小李不参与核心模块开发。这是典型的否定后件推否定前件的逻辑推理。");
        } else if ("english".equals(type)) {
            result.put("correctAnswer", "C. for");
            result.put("reasoning", "\"be responsible for\" 是固定搭配，表示“负责......”。");
        } else {
            result.put("correctAnswer", "未知题型");
            result.put("reasoning", "暂无分析思路。");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 清空当前用户的所有面试记录
     */
    @DeleteMapping("/api/records/clear")
    @ResponseBody
    public ResponseEntity<?> clearAllRecords(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("用户未找到"));
        
        try {
            interviewRecordRepository.deleteByUserId(user.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "清空成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "清空失败: " + e.getMessage()));
        }
    }
}
