package com.interviewai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewai.entity.InterviewRecord;
import com.interviewai.repository.InterviewRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncReportService {

    @Autowired
    private LlmService llmService;

    @Autowired
    private InterviewRecordRepository interviewRecordRepository;

    @Async
    public void generateAsyncReport(Long recordId, String questionTitle, String standardAnswer, String personaPrompt, String chatHistoryJson) {
        boolean hasUserReply = false;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode chatArray = mapper.readTree(chatHistoryJson);
            if (chatArray.isArray()) {
                for (JsonNode node : chatArray) {
                    if (node.has("role") && "user".equals(node.get("role").asText())) {
                        if (node.has("content") && !node.get("content").asText().trim().isEmpty()) {
                            hasUserReply = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (chatHistoryJson != null && (chatHistoryJson.contains("\"role\":\"user\"") || chatHistoryJson.contains("\"role\": \"user\""))) {
                hasUserReply = true;
            }
        }

        String llmJsonResponse;
        if (!hasUserReply) {
            llmJsonResponse = "{\n" +
                    "  \"score\": 0,\n" +
                    "  \"foundationScore\": 0,\n" +
                    "  \"logicScore\": 0,\n" +
                    "  \"communicationScore\": 0,\n" +
                    "  \"stressScore\": 0,\n" +
                    "  \"techScore\": 0,\n" +
                    "  \"businessScore\": 0,\n" +
                    "  \"starAnalysis\": {\n" +
                    "    \"situation\": \"由于您未进行任何回答就结束了面试，无法分析情景。\",\n" +
                    "    \"task\": \"无任务表现。\",\n" +
                    "    \"action\": \"无行动表现。\",\n" +
                    "    \"result\": \"无结果数据。\"\n" +
                    "  },\n" +
                    "  \"feedbackReport\": \"【评估报告】您在本次模拟面试中未回答任何问题就直接结束了面试，因此所有维度评分均为 0。\",\n" +
                    "  \"suggestions\": \"【建议】请在开始面试后，积极通过语音或文本输入回答面试官提出的问题，以便 AI 进行多维度能力评估。\"\n" +
                    "}";
        } else {
            // 调用大模型服务，获得结构化 JSON 反馈 (此时在独立线程中执行，不会阻塞前端)
            llmJsonResponse = llmService.evaluateSession(questionTitle, standardAnswer, personaPrompt, chatHistoryJson);
        }

        InterviewRecord record = interviewRecordRepository.findById(recordId).orElse(null);
        if (record == null) return;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(llmJsonResponse);
            
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

        // 保存完整的评估结果到数据库
        interviewRecordRepository.save(record);
    }
}
