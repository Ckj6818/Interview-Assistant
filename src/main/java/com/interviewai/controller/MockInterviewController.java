package com.interviewai.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.entity.InterviewRecord;
import com.interviewai.entity.Question;
import com.interviewai.entity.User;
import com.interviewai.repository.InterviewRecordRepository;
import com.interviewai.repository.QuestionRepository;
import com.interviewai.repository.UserRepository;
import com.interviewai.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mock-interview")
public class MockInterviewController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewRecordRepository interviewRecordRepository;

    @Autowired
    private LlmService llmService;

    /**
     * 进入全真模拟面试室页面
     */
    @GetMapping
    public String enterMockInterviewRoom() {
        return "mock_interview";
    }

    /**
     * 接收前台的聊天记录数组，调用大模型返回 AI 的下一句回复
     */
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, String>> messages = mapper.convertValue(
                    payload.get("messages"),
                    new TypeReference<List<Map<String, String>>>() {}
            );

            // 如果是第一轮，注入 System Prompt
            if (messages.isEmpty() || !messages.get(0).get("role").equals("system")) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                
                String resume = payload.containsKey("resume") ? payload.get("resume").toString() : "";
                String targetCompany = payload.containsKey("company") ? payload.get("company").toString() : "";
                String targetJob = payload.containsKey("job") ? payload.get("job").toString() : "";
                
                StringBuilder systemPrompt = new StringBuilder();
                systemPrompt.append("你是一位资深、严苛的技术面试官。本次面试没有固定题库，而是全真模拟真实环境。\n");
                
                if (targetCompany != null && !targetCompany.trim().isEmpty()) {
                    systemPrompt.append("候选人申请的目标公司是：[" + targetCompany + "]。\n");
                }
                if (targetJob != null && !targetJob.trim().isEmpty()) {
                    systemPrompt.append("候选人申请的目标岗位是：[" + targetJob + "]。\n");
                } else {
                    systemPrompt.append("候选人正在进行综合技术面试。\n");
                }
                
                if (resume != null && !resume.trim().isEmpty()) {
                    systemPrompt.append("候选人的简历内容如下：\n---\n" + resume + "\n---\n请结合简历内容对候选人的相关项目进行深度挖掘、场景假设和针对性的追问。\n");
                } else {
                    systemPrompt.append("请围绕目标岗位相关的核心技术栈、系统设计和常见八股文进行考察。\n");
                }
                
                systemPrompt.append("要求：\n" +
                        "1. 你的话必须精炼，口语化，适合通过语音播报。千万不要使用Markdown排版。\n" +
                        "2. 每次只抛出一个问题或追问，千万不要长篇大论。\n" +
                        "3. 如果候选人回答错误，适度指出并要求他重新思考。\n" +
                        "4. 如果候选人请求结束，你可以做一段简短总结。");
                
                systemMsg.put("content", systemPrompt.toString());
                messages.add(0, systemMsg);
            }

            String aiReply = llmService.chatInterviewRound(messages);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reply", aiReply
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 流式交互接口：接收新对话并返回 SSE 流
     */
    @PostMapping(value = "/chat-stream", produces = "text/event-stream")
    @ResponseBody
    public reactor.core.publisher.Flux<String> chatInterviewStream(@RequestBody Map<String, Object> payload) {
        try {
            List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");

            // 如果是第一轮，注入 System Prompt
            if (messages.isEmpty() || !messages.get(0).get("role").equals("system")) {
                Map<String, String> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                
                String resume = payload.containsKey("resume") ? payload.get("resume").toString() : "";
                String targetCompany = payload.containsKey("company") ? payload.get("company").toString() : "";
                String targetJob = payload.containsKey("job") ? payload.get("job").toString() : "";
                
                StringBuilder systemPrompt = new StringBuilder();
                systemPrompt.append("你是一位资深、严苛的技术面试官。本次面试没有固定题库，而是全真模拟真实环境。\n");
                
                if (targetCompany != null && !targetCompany.trim().isEmpty()) {
                    systemPrompt.append("候选人申请的目标公司是：[" + targetCompany + "]。\n");
                }
                if (targetJob != null && !targetJob.trim().isEmpty()) {
                    systemPrompt.append("候选人申请的目标岗位是：[" + targetJob + "]。\n");
                } else {
                    systemPrompt.append("候选人正在进行综合技术面试。\n");
                }
                
                if (resume != null && !resume.trim().isEmpty()) {
                    systemPrompt.append("候选人的简历内容如下：\n---\n" + resume + "\n---\n请结合简历内容对候选人的相关项目进行深度挖掘、场景假设和针对性的追问。\n");
                } else {
                    systemPrompt.append("请围绕目标岗位相关的核心技术栈、系统设计和常见八股文进行考察。\n");
                }
                
                systemPrompt.append("要求：\n" +
                        "1. 你的话必须精炼，口语化，适合通过语音播报。千万不要使用Markdown排版。\n" +
                        "2. 每次只抛出一个问题或追问，千万不要长篇大论。\n" +
                        "3. 如果候选人回答错误，适度指出并要求他重新思考。\n" +
                        "4. 如果候选人请求结束，你可以做一段简短总结。");
                
                systemMsg.put("content", systemPrompt.toString());
                messages.add(0, systemMsg);
            }

            return llmService.chatInterviewRoundStream(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return reactor.core.publisher.Flux.just("【系统异常】无法建立语音流连接，请重试。");
        }
    }

    /**
     * 结束面试，保存全量聊天记录并生成总评
     */
    @PostMapping("/finish")
    @ResponseBody
    public ResponseEntity<?> finishInterview(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        
        try {
            String chatLogStr = payload.get("chatLog").toString(); // 前端把全部对话拼接为文本
            String resume = payload.containsKey("resume") ? payload.get("resume").toString() : "";
            String targetCompany = payload.containsKey("company") ? payload.get("company").toString() : "";
            String targetJob = payload.containsKey("job") ? payload.get("job").toString() : "";

            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

            StringBuilder evaluationContext = new StringBuilder();
            if (resume != null && !resume.trim().isEmpty()) {
                evaluationContext.append("候选人简历上下文：\n").append(resume).append("\n");
            }
            if (targetCompany != null && !targetCompany.trim().isEmpty()) {
                evaluationContext.append("目标公司：").append(targetCompany).append("\n");
            }
            if (targetJob != null && !targetJob.trim().isEmpty()) {
                evaluationContext.append("目标岗位：").append(targetJob).append("\n");
            }

            String topicName = targetJob != null && !targetJob.trim().isEmpty() ? targetJob + " 综合面试" : "全真模拟综合面试";

            // 检查是否有候选人的作答记录
            boolean hasCandidateReplies = chatLogStr != null && chatLogStr.contains("候选人:");
            String llmJsonResponse;
            if (!hasCandidateReplies) {
                llmJsonResponse = "{\n" +
                        "  \"score\": 0,\n" +
                        "  \"foundationScore\": 0,\n" +
                        "  \"logicScore\": 0,\n" +
                        "  \"communicationScore\": 0,\n" +
                        "  \"stressScore\": 0,\n" +
                        "  \"techScore\": 0,\n" +
                        "  \"businessScore\": 0,\n" +
                        "  \"starAnalysis\": {\n" +
                        "    \"situation\": \"由于候选人直接结束了面试且未作答，无法评估情景。\",\n" +
                        "    \"task\": \"无任务表现。\",\n" +
                        "    \"action\": \"无行动表现。\",\n" +
                        "    \"result\": \"无结果数据。\"\n" +
                        "  },\n" +
                        "  \"feedbackReport\": \"【评估报告】您在本次模拟面试中未回答任何问题就直接结束了面试，因此所有维度评分均为 0。\",\n" +
                        "  \"suggestions\": \"【建议】请在开始面试后，积极通过语音或文本输入回答面试官提出的问题，以便 AI 进行多维度能力评估。\"\n" +
                        "}";
            } else {
                // 生成最终评价
                String prompt = "你是一位资深技术面试官。以下是候选人与你的完整面试对话记录：\n" + chatLogStr;
                llmJsonResponse = llmService.evaluateAnswer(
                        topicName, 
                        prompt, 
                        "无固定参考答案，考察综合素质", 
                        evaluationContext.toString()
                );
            }

            InterviewRecord record = new InterviewRecord();
            record.setUser(user);
            record.setTopic(topicName);
            record.setStandardAnswer("无固定参考答案");
            record.setChatLog(chatLogStr); // 保存完整的对话上下文

            ObjectMapper mapper = new ObjectMapper();
            try {
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(llmJsonResponse);
                record.setScore(root.has("score") ? root.get("score").asInt() : 0);
                record.setFoundationScore(root.has("foundationScore") ? root.get("foundationScore").asInt() : 0);
                record.setLogicScore(root.has("logicScore") ? root.get("logicScore").asInt() : 0);
                record.setCommunicationScore(root.has("communicationScore") ? root.get("communicationScore").asInt() : 0);
                record.setStressScore(root.has("stressScore") ? root.get("stressScore").asInt() : 0);
                record.setTechScore(root.has("techScore") ? root.get("techScore").asInt() : 0);
                record.setBusinessScore(root.has("businessScore") ? root.get("businessScore").asInt() : 0);
                
                if (root.has("starAnalysis")) {
                    record.setStarAnalysisJson(mapper.writeValueAsString(root.get("starAnalysis")));
                }
                
                record.setFeedbackReport(root.has("feedbackReport") ? root.get("feedbackReport").asText() : "暂无评价");
                record.setSuggestions(root.has("suggestions") ? root.get("suggestions").asText() : "");
            } catch (Exception e) {
                record.setScore(0);
                record.setFeedbackReport(llmJsonResponse);
            }

            interviewRecordRepository.save(record);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "recordId", record.getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false));
        }
    }
}
