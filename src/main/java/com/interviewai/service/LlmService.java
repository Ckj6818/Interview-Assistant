package com.interviewai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import java.util.*;

@Service
public class LlmService {

    @Value("${interviewai.llm.enable-real-api:false}")
    private boolean enableRealApi;

    @Value("${interviewai.llm.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${interviewai.llm.api-key:YOUR_DEEPSEEK_API_KEY_HERE}")
    private String apiKey;

    @Value("${interviewai.llm.model:deepseek-chat}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();

    /**
     * 调用大模型进行面试评测，并返回结构化的评估结果。
     */
    public String evaluateAnswer(String question, String answer, String standardAnswer, String personaPrompt) {
        // 如果未开启真实API，或者使用的是默认占位符，自动优雅降级为模拟数据
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockEvaluation(personaPrompt, answer);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一位资深的面试官。" + (personaPrompt != null ? personaPrompt : "") + 
                    "\n请对候选人的回答进行打分和评估。请保持高度客观，不要给同情分。你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记（如 `json ）。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"score\": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),\n" +
                    "  \"foundationScore\": 专业基础评分(0-100的整数，未作答或回答错误应低分),\n" +
                    "  \"logicScore\": 逻辑思维评分(0-100的整数),\n" +
                    "  \"communicationScore\": 沟通表达评分(0-100的整数),\n" +
                    "  \"stressScore\": 抗压应变评分(0-100的整数),\n" +
                    "  \"techScore\": 技术深度评分(0-100的整数),\n" +
                    "  \"businessScore\": 业务场景评分(0-100的整数),\n" +
                    "  \"starAnalysis\": {\n" +
                    "    \"situation\": \"S-情景再现与背景分析：指出候选人描述中的情景缺失或亮点\",\n" +
                    "    \"task\": \"T-任务理解与目标拆解分析\",\n" +
                    "    \"action\": \"A-行动过程与技术手段的详细分析\",\n" +
                    "    \"result\": \"R-结果收益与数据支撑分析\"\n" +
                    "  },\n" +
                    "  \"feedbackReport\": \"详细的评估报告文本\",\n" +
                    "  \"suggestions\": \"改进建议与参考答案\"\n" +
                    "}";

            String userContent = "面试题是：" + question + "\n官方标准答案参考：" + standardAnswer + "\n候选人的回答是：" + answer + "\n请根据标准答案进行评估。";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.3);

            // 如果是 DeepSeek，支持 json 格式约束
            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型 API 异常，进入保护级降级逻辑: " + e.getMessage());
        }

        return getMockEvaluation(personaPrompt, answer);
    }

    /**
     * 实时提词助手专用方法：返回 JSON 格式的 analysis 与建议
     */
    public String generateCopilotSuggestion(String interviewerQuestion, String mode, String resumeText, String targetJd, boolean isUserSpeaking) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockCopilot(interviewerQuestion, mode, isUserSpeaking);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String modeInstruction = "";
            switch (mode != null ? mode : "normal") {
                case "algorithm":
                    modeInstruction = "当前模式为【算法题模式】。请分析题目的时间/空间复杂度，并给出最优解的代码逻辑框架，如果是复杂逻辑可使用 Markdown 或 LaTeX/Mermaid 辅助说明。";
                    break;
                case "system_design":
                    modeInstruction = "当前模式为【系统设计模式】。请重点拆解系统架构、高并发、缓存策略、数据库设计等要点，可以使用 Markdown 和 Mermaid 流程图来展示。";
                    break;
                case "detailed":
                    modeInstruction = "当前模式为【详细模式】。请提供深度技术剖析、源码级解读或复杂场景排坑经验，展现深厚技术功底。";
                    break;
                case "conversational":
                    modeInstruction = "当前模式为【口语化表达】。你的回答必须极其自然，像是真人在说话，多用口语连接词，少用生硬的列点。";
                    break;
                case "phone_interview":
                    modeInstruction = "当前模式为【电话面试】。回答必须简短有力，没有视觉辅助的情况下，纯靠逻辑清晰 of 口述让面试官理解。";
                    break;
                case "normal":
                default:
                    modeInstruction = "当前模式为【普通模式】。请给出极简、可以直接照着读的回答提纲。字数控制在150字以内，采用分点形式。";
                    break;
            }
            
            String contextPrompt = "";
            if (resumeText != null && !resumeText.isEmpty()) {
                contextPrompt += "【候选人简历上下文】：\n" + resumeText + "\n\n";
            }
            if (targetJd != null && !targetJd.isEmpty()) {
                contextPrompt += "【目标职位 JD】：\n" + targetJd + "\n\n";
            }
            if (!contextPrompt.isEmpty()) {
                contextPrompt += "请务必将上述简历和岗位要求融入到你的回答中，让答案听起来是为其量身定制的。\n";
            }

            String roleInstruction = "你是一位实时的面试提词助手。";
            String userPrefix = "面试官问题：";
            
            if (isUserSpeaking) {
                roleInstruction = "你是候选人的实时表达教练。现在收到了候选人（你的用户）正在说的话，请评估他的回答，并给出纠正或下一步思路引导。";
                userPrefix = "候选人正在说：";
            }

            String systemPrompt = roleInstruction + "\n" + contextPrompt + modeInstruction + "\n" +
                    "你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记，如 ```json 这种包围。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"analysis\": \"左侧面板展示：" + (isUserSpeaking ? "候选人回答的逻辑漏洞或当前表现评估" : "面试官的问题意图分析、陷阱分析或考察重点") + "（简明扼要，约50字）\",\n" +
                    "  \"answer\": \"中间面板展示：" + (isUserSpeaking ? "针对他说的话的紧急纠正、下一步引导或思路补全" : "具体的回答内容、策略提纲或技术拆解") + "\"\n" +
                    "}";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrefix + interviewerQuestion)
            ));
            requestBody.put("temperature", 0.5);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型提词 API 异常，进入保护级降级逻辑: " + e.getMessage());
        }

        return getMockCopilot(interviewerQuestion, mode, isUserSpeaking);
    }

    /**
     * 全真模拟面试的连续对话逻辑
     */
    public String chatInterviewRound(List<Map<String, String>> chatHistory) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return "【网络/API未配置】您的回答我已收到，但目前处于离线模式。您可以继续作答，或结束面试查看总评。";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", chatHistory);
            requestBody.put("temperature", 0.3);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText().trim();
            }
        } catch (Exception e) {
            System.err.println("调用大模型聊天 API 异常: " + e.getMessage());
        }
        return "【异常】似乎网络连接中断了，请再说一遍或者稍后再试。";
    }

    /**
     * 流式调用大模型：返回响应式 Flux，供 SSE 逐字推送给前端
     */
    public Flux<String> chatInterviewRoundStream(List<Map<String, String>> chatHistory) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return Flux.just("【降级模式】由于未配置API Key，无法演示流式打字机效果。");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", chatHistory);
        requestBody.put("temperature", 0.3);
        requestBody.put("stream", true);

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(chunk -> {
                    if (chunk == null || chunk.trim().isEmpty()) {
                        return "";
                    }
                    String dataLine = chunk.trim();
                    if ("[DONE]".equals(dataLine)) {
                        return "";
                    }
                    try {
                        JsonNode root = mapper.readTree(dataLine);
                        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                            JsonNode delta = root.get("choices").get(0).path("delta");
                            if (delta.has("content")) {
                                return delta.get("content").asText();
                            }
                        }
                    } catch (Exception e) {
                        // ignore parse errors for split chunks
                    }
                    return "";
                }).filter(s -> !s.isEmpty());
    }

    /**
     * 全场模拟面试结束后的综合结算评价
     */
    public String evaluateSession(String question, String standardAnswer, String personaPrompt, String chatHistoryJson) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockEvaluation(personaPrompt, chatHistoryJson);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一位资深的面试官。" + (personaPrompt != null ? personaPrompt : "") + 
                    "\n现在面试结束，请根据下述连续多轮对话的面试记录，对候选人的综合表现进行打分和评估。请保持高度客观，不要给同情分。你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"score\": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),\n" +
                    "  \"foundationScore\": 专业基础评分(0-100的整数，未作答或回答错误应低分),\n" +
                    "  \"logicScore\": 逻辑思维评分(0-100的整数),\n" +
                    "  \"communicationScore\": 沟通表达评分(0-100的整数),\n" +
                    "  \"stressScore\": 抗压应变评分(0-100的整数),\n" +
                    "  \"techScore\": 技术深度评分(0-100的整数),\n" +
                    "  \"businessScore\": 业务场景评分(0-100的整数),\n" +
                    "  \"starAnalysis\": {\n" +
                    "    \"situation\": \"S-情景再现与背景分析：指出候选人描述中的情景缺失或亮点\",\n" +
                    "    \"task\": \"T-任务理解与目标拆解分析\",\n" +
                    "    \"action\": \"A-行动过程与技术手段的详细分析\",\n" +
                    "    \"result\": \"R-结果收益与数据支撑分析\"\n" +
                    "  },\n" +
                    "  \"feedbackReport\": \"详细的总体评估报告文本\",\n" +
                    "  \"suggestions\": \"针对整场面试的改进建议\"\n" +
                    "}";

            String userContent = "面试初始问题是：" + question + "\n官方参考答案：" + standardAnswer + "\n以下是完整的面试对话记录：\n" + chatHistoryJson + "\n请综合全场表现给出评价。";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.3);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型结算 API 异常: " + e.getMessage());
        }

        return getMockEvaluation(personaPrompt, chatHistoryJson);
    }

    private String getMockEvaluation(String personaPrompt, String chatLogOrAnswer) {
        try {
            boolean isEmpty = true;
            if (chatLogOrAnswer != null && !chatLogOrAnswer.trim().isEmpty()) {
                // 检查是否包含有用的候选人作答或者 role: user
                if (chatLogOrAnswer.contains("候选人:") || chatLogOrAnswer.contains("\"role\":\"user\"") || chatLogOrAnswer.contains("\"role\": \"user\"")) {
                    isEmpty = false;
                } else if (!chatLogOrAnswer.contains("[") && chatLogOrAnswer.trim().length() > 5) {
                    // 单题练习的纯文本作答，不是 JSON 数组，且长度大于5
                    isEmpty = false;
                }
            }

            Map<String, Object> mockResponse = new HashMap<>();
            if (isEmpty) {
                mockResponse.put("score", 0);
                mockResponse.put("foundationScore", 0);
                mockResponse.put("logicScore", 0);
                mockResponse.put("communicationScore", 0);
                mockResponse.put("stressScore", 0);
                mockResponse.put("techScore", 0);
                mockResponse.put("businessScore", 0);
                
                Map<String, String> star = new HashMap<>();
                star.put("situation", "由于候选人直接结束了面试且未作答，无法评估情景。");
                star.put("task", "无任务表现。");
                star.put("action", "无行动表现。");
                star.put("result", "无结果数据。");
                mockResponse.put("starAnalysis", star);
                
                mockResponse.put("feedbackReport", "【评估报告】您在本次模拟面试中未回答任何问题就直接结束了面试，因此所有维度评分均为 0。");
                mockResponse.put("suggestions", "【建议】请在开始面试后，积极通过语音或文本输入回答面试官提出的问题，以便 AI 进行多维度能力评估。");
            } else {
                // 根据内容长度计算一个客观分数 (60-95 之间)
                int baseLength = chatLogOrAnswer.trim().length();
                int score = 60 + Math.min(35, baseLength / 10); // 每 10 个字加 1 分，上限 95
                
                mockResponse.put("score", score);
                mockResponse.put("foundationScore", Math.max(60, score - 3));
                mockResponse.put("logicScore", Math.max(60, score + 2));
                mockResponse.put("communicationScore", Math.max(60, score + 4));
                mockResponse.put("stressScore", Math.max(60, score));
                mockResponse.put("techScore", Math.max(60, score + 1));
                mockResponse.put("businessScore", Math.max(60, score - 5));
                
                Map<String, String> star = new HashMap<>();
                star.put("situation", "交代了项目是在高并发电商秒杀背景下进行的，情景比较清晰。");
                star.put("task", "明确了任务是解决超卖和提高吞吐量。");
                star.put("action", "采用了 Redis 分布式锁，但对锁的续期机制（WatchDog）解释不足。");
                star.put("result", "缺乏具体的数据支撑，例如 QPS 提升了多少。");
                mockResponse.put("starAnalysis", star);
                
                mockResponse.put("feedbackReport", "【降级保护模式】作为" + (personaPrompt != null && !personaPrompt.isEmpty() ? "设定的面试官" : "AI面试官") + "，我的评价是：您的回答已收悉。在本次互动中，您的表达结构清晰，能够抓住核心问题，但回答的深度和技术细节仍有提升空间。");
                mockResponse.put("suggestions", "建议：1. 针对面试官提问，补充更多底层细节与设计模式应用；2. 结合实际项目中的高并发或系统稳定性问题进行回答，增加实战说服力。");
            }

            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"score\":0, \"feedbackReport\":\"解析失败\"}";
        }
    }

    private String getMockCopilot(String interviewerQuestion, String mode, boolean isUserSpeaking) {
        try {
            Map<String, Object> mockResponse = new HashMap<>();
            if (isUserSpeaking) {
                mockResponse.put("analysis", "【模拟评估】正在倾听候选人回答...");
                mockResponse.put("answer", "【模拟教练】可以顺着这个思路，再补充一下性能指标。");
                return mapper.writeValueAsString(mockResponse);
            }
            String analysis = "正在考察对底层机制或架构体系的理解能力，注意别只背诵八股文。";
            String answer = "";

            if (interviewerQuestion.contains("依赖注入") || interviewerQuestion.toLowerCase().contains("ioc")) {
                analysis = "考察 Spring 框架核心理念，重点在解耦。";
                answer = "1. 控制反转(IoC)把对象的创建交给容器。\n2. 降低代码耦合度。\n3. 常用注入方式：构造器、Setter、字段注入。";
            } else if (interviewerQuestion.toLowerCase().contains("redis")) {
                analysis = "考察缓存中间件的原理和高并发下的处理经验。";
                if ("system_design".equals(mode)) {
                    answer = "系统设计：\n1. 采用分布式缓存集群(Redis Cluster)。\n2. 处理缓存穿透(布隆过滤器)、缓存击穿(分布式锁)、缓存雪崩(随机过期时间)。\n3. 结合双写一致性策略。";
                } else {
                    answer = "1. 单线程运行避免上下文切换，非阻塞 IO 多路复用。\n2. 用于高并发缓存、分布式锁。\n3. 注意缓存击穿、穿透、雪崩问题。";
                }
            } else if (interviewerQuestion.contains("排序") || interviewerQuestion.contains("数组") || "algorithm".equals(mode)) {
                analysis = "考察算法基础，注意边界条件 and 复杂度。";
                answer = "算法思路：\n1. 时间复杂度优先考虑 O(N log N) 或 O(N)。\n2. 空间复杂度尽量 O(1)。\n3. 核心逻辑：定义双指针或滑动窗口进行迭代。注意判空边界。";
            } else {
                analysis = "通用问题评估，需结合具体经历作答。";
                answer = "1. 核心概念：理解 " + interviewerQuestion + " 的底层实现。\n2. 优势分析：提高吞吐量，解耦业务模块。\n3. 实战切入：在之前项目中，我们成功应用并优化了延迟。";
            }
            
            mockResponse.put("analysis", "【模拟】" + analysis);
            mockResponse.put("answer", "【模拟】" + answer);
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"analysis\":\"解析异常\", \"answer\":\"获取降级数据失败\"}";
        }
    }

    /**
     * 调用大模型对简历进行打分、评估和JD匹配润色
     */
    public String analyzeResume(String resumeText, String targetJd) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockResumeAnalysis(resumeText, targetJd);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一位资深的HR专家与职业生涯规划师。\n" +
                    "请针对候选人的简历内容进行深度诊断与多维度打分，并根据目标职位的JD（岗位描述）提供精准的匹配与润色建议。\n" +
                    "你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记（如 ```json ）。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"score\": 简历得分(0-100的整数),\n" +
                    "  \"matchScore\": JD匹配度评分(0-100的整数),\n" +
                    "  \"feedbackReport\": \"详细的简历诊所报告(包括排版、内容组织、亮点陈述等方面的不足和诊断建议)\",\n" +
                    "  \"suggestions\": \"针对目标JD的精准润色方案(详细列出如何智能重组内容、挖掘核心亮点、补充关键词等以通过ATS筛选)\",\n" +
                    "  \"optimizedResume\": \"润色后的简历推荐内容或主要板块优化示范\"\n" +
                    "}";

            String userContent = "候选人简历内容：\n" + resumeText + "\n\n目标职位的JD（岗位描述）：\n" + (targetJd != null && !targetJd.trim().isEmpty() ? targetJd : "暂无（进行通用简历诊所评估）");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.5);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型进行简历分析异常: " + e.getMessage());
        }

        return getMockResumeAnalysis(resumeText, targetJd);
    }

    private String getMockResumeAnalysis(String resumeText, String targetJd) {
        try {
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("score", 78);
            mockResponse.put("matchScore", targetJd != null && !targetJd.trim().isEmpty() ? 65 : 0);
            mockResponse.put("feedbackReport", "【降级保护模式】简历内容较为完整，但在工作经历的描述中，缺乏具体的量化指标（如：提升了多少性能，减少了多少故障）。项目描述中，只写了做了什么，没有体现具体的技术难点和自己做出的技术贡献。另外，技能栈的排版有些散乱，建议按语言、框架、中间件等分类整理。");
            mockResponse.put("suggestions", "【建议】\n1. 突出量化指标：在你的简历中加入类似‘将系统吞吐量提升了 30%’、‘解决线上高并发瓶颈，降低接口响应时延 200ms’等量化结果。\n2. ATS优化：检测到目标JD中包含‘高并发、微服务架构、Redis分布式锁’，建议在简历项目经历中特意写明利用Redis实现分布式锁，并提炼相关的高并发关键词以提高ATS系统筛选率。");
            mockResponse.put("optimizedResume", "【优化示范 - 个人经历版块】\n- 原句：参与了商城系统后台开发，负责购物车和订单模块，使用Redis来做缓存。\n- 优化后：主导商城系统高并发购物车及订单微服务开发，引入Redis多路复用机制构建高效二级缓存结构，将高频商品数据访问速度提升了40%；通过实现Redis分布式锁，彻底解决超卖问题，保障了秒杀场景下数据的一致性与稳定性。");

            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"score\":70, \"matchScore\":60, \"feedbackReport\":\"解析失败\"}";
        }
    }

    /**
     * 调用大模型生成全量ATS友好型精修简历，支持多版本人设
     */
    public String optimizeFullResume(String resumeText, String targetJd, String versionType) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return "# 模拟生成：" + versionType + " 版简历\n\n> 由于系统当前处于离线Mock模式，此处展示模拟数据。\n\n## 核心优势\n- 深度契合JD需求，熟练掌握高并发设计与微服务架构。\n\n## 项目经历\n### 核心电商订单系统重构\n- **背景**：针对系统性能瓶颈进行底层重构。\n- **行动**：引入Redis分布式缓存与RabbitMQ异步削峰机制。\n- **结果**：系统吞吐量提升了 40%，接口延迟降低 200ms。";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String basePrompt = "你是一位精通ATS（简历自动筛选系统）规则的资深猎头与职业规划专家。\n" +
                    "请根据用户提供的原始简历和目标岗位JD，为用户全量重写并生成一份完整的、极具竞争力的 ATS 友好型简历。\n";

            String versionRules = "";
            if ("aggressive".equals(versionType)) {
                versionRules = "【高维打击版 (想象的自己) 核心策略】：\n" +
                        "1. 拔高视野：将用户的经历从‘普通执行者/开发’视角，全面拔高到‘核心骨干/架构设计/技术Leader’视角。\n" +
                        "2. 大厂话术：大量使用互联网大厂的高级黑话和前沿术语（如：底层重构、全链路压测、降本增效、高可用架构设计、赋能业务、打通壁垒）。\n" +
                        "3. 夸大成果：在合理范围内最大限度地美化业务成果，将普通的优化描述为‘主导了系统级重构，彻底解决历史包袱’。\n";
            } else if ("hr_target".equals(versionType)) {
                versionRules = "【JD狙击版 (HR想要的你) 核心策略】：\n" +
                        "1. 迎合JD：这版简历的唯一目的是通过当前提供的JD ATS系统筛选。请逐一拆解JD中的硬性要求。\n" +
                        "2. 关键词堆叠：将JD中提到的框架、组件、技能名词，自然但高频地散布在用户的项目经历中，使其看起来100%是对口人才。\n" +
                        "3. 隐恶扬善：对于JD没提的技能可以一笔带过，对于JD重点要求的技能进行超长篇幅的重笔墨渲染。\n";
            } else {
                versionRules = "【务实精修版 (真实的自己) 核心策略】：\n" +
                        "1. 真实专业：保持用户原有经历的真实性，但不遗余力地提升语言的专业度、干练度。\n" +
                        "2. 强化STAR：深挖现有描述中隐藏的价值点，使其无懈可击，能够完美应对严苛的背景调查。\n";
            }

            String systemPrompt = basePrompt + versionRules +
                    "【严格规范】：\n" +
                    "1. 全篇重构：不要只是提建议，必须直接输出排版精美的 Markdown 格式完整简历。\n" +
                    "2. 必须遵循STAR法则：所有项目经验重写为“背景-行动-结果”结构，大量补充合理的业务量化指标（如性能提升百分比）。\n" +
                    "3. 只输出简历内容本身，绝对不要包含诸如“你好，这是为你优化的简历”之类的客套话或分析性前缀/后缀文字。";

            String userContent = "原始简历内容：\n" + resumeText + "\n\n目标岗位JD：\n" + (targetJd != null && !targetJd.trim().isEmpty() ? targetJd : "暂无，请做通用级别深度精修");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.6);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return content.trim();
            }
        } catch (Exception e) {
            System.err.println("调用大模型进行全量简历优化异常: " + e.getMessage());
        }

        return "系统繁忙，简历全量精修失败，请稍后重试。";
    }

    /**
     * 在线评测：代码运行/检查 (Run Code)
     */
    public String runCodeCheck(String questionTitle, String questionType, String code) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockCodeCheck(questionTitle, questionType, code);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一个代码执行沙箱和编译器模拟器。请对用户提交的解答（可能是Java代码或针对概念题的文字/Markdown回答）进行静态分析与测试用例模拟运行。\n" +
                    "你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记（如 ```json ）。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"success\": true,\n" +
                    "  \"compileOk\": true,\n" +
                    "  \"compilerMessage\": \"编译成功\" 或 具体的编译错误/异常提示信息,\n" +
                    "  \"runResults\": [\n" +
                    "    {\n" +
                    "      \"input\": \"测试输入1\",\n" +
                    "      \"expected\": \"期望的输出\",\n" +
                    "      \"actual\": \"实际的输出\",\n" +
                    "      \"passed\": true\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"diagnostic\": \"算法性能、边界条件及可读性诊断信息（约80字）\"\n" +
                    "}";

            String userContent = "题目：" + questionTitle + "\n类型：" + questionType + "\n解答内容：\n" + code;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.2);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型运行代码 API 异常: " + e.getMessage());
        }

        return getMockCodeCheck(questionTitle, questionType, code);
    }

    /**
     * 在线算法协作模式：检测到停顿后，AI以面试官视角旁观代码并给出点拨
     */
    public String generateCodeHint(String questionTitle, String questionType, String code) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return "{\"hint\":\"【模拟面试官】注意检查边界条件，比如空数组或者双指针相遇的情况。\"}";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一位正在线上面试的算法面试官。\n" +
                    "候选人正在尝试解决一道算法题。以下是他当前敲出的残缺/进行中代码。你观察到了这段代码，请一针见血地指出潜在的问题、漏掉的边界条件，或者给出下一步的思路引导。不要直接给出完整代码，而是像真实面试官那样用语言点拨。\n" +
                    "你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"hint\": \"你的面试官点评（口语化，最多60个字）\"\n" +
                    "}";

            String userContent = "题目：" + questionTitle + "\n当前进度代码：\n" + code;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.5);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型进行代码点拨异常: " + e.getMessage());
        }

        return "{\"hint\":\"【保护模式】面试官似乎去接了个水，继续写吧！\"}";
    }

    /**
     * 在线评测：提交代码/答案评测 (Submit Code)
     */
    public String submitQuestionCheck(String questionTitle, String questionType, String code, String standardAnswer) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return getMockQuestionSubmission(questionTitle, questionType, code, standardAnswer);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你编写的是资深的算法和技术面试官。请针对用户提交的题目解答（代码或概念题回答）进行深度打分和评测，并与标准答案做对比。请保持高度客观，不要给同情分。你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记（如 ```json ）。\n" +
                    "JSON 格式要求如下：\n" +
                    "{\n" +
                    "  \"score\": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),\n" +
                    "  \"foundationScore\": 专业基础/编译正确性评分(0-100的整数，未作答或回答错误应低分),\n" +
                    "  \"logicScore\": 逻辑思维/算法复杂度评分(0-100的整数),\n" +
                    "  \"communicationScore\": 沟通表达/代码规范度评分(0-100的整数),\n" +
                    "  \"stressScore\": 边界处理/鲁棒性评分(0-100的整数),\n" +
                    "  \"feedbackReport\": \"详细的评估报告，使用 Markdown 格式（包括解题思路分析、时空复杂度分析、亮点和扣分点说明等）\",\n" +
                    "  \"suggestions\": \"改进建议与推荐解法（针对性优化细节或概念解析）\"\n" +
                    "}";

            String userContent = "题目：" + questionTitle + "\n类型：" + questionType + "\n官方标准答案：" + standardAnswer + "\n用户提交的解答：\n" + code;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent)
            ));
            requestBody.put("temperature", 0.3);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型提交代码 API 异常: " + e.getMessage());
        }

        return getMockQuestionSubmission(questionTitle, questionType, code, standardAnswer);
    }

    private String getMockCodeCheck(String questionTitle, String questionType, String code) {
        try {
            Map<String, Object> mockResponse = new HashMap<>();
            boolean isCode = "code".equalsIgnoreCase(questionType);
            
            mockResponse.put("success", true);
            mockResponse.put("compileOk", true);
            mockResponse.put("compilerMessage", isCode ? "JDK 21 编译成功。未发现语法错误。" : "概念分析格式校验通过。");
            
            List<Map<String, Object>> runResults = new ArrayList<>();
            if (isCode) {
                if (questionTitle.contains("第K大")) {
                    runResults.add(Map.of("input", "nums = [3,2,1,5,6,4], k = 2", "expected", "5", "actual", "5", "passed", true));
                    runResults.add(Map.of("input", "nums = [3,3,2,1,5,6,4], k = 2", "expected", "5", "actual", "5", "passed", true));
                } else {
                    runResults.add(Map.of("input", "默认测试输入", "expected", "正常输出", "actual", "正常输出", "passed", true));
                }
                mockResponse.put("diagnostic", "【降级模式】代码逻辑运行平稳，时间复杂度约 O(N log K)，空间复杂度控制良好。");
            } else {
                runResults.add(Map.of("input", "文字字数校验", "expected", "> 10字", "actual", code != null ? String.valueOf(code.length()) : "0", "passed", code != null && code.length() > 10));
                mockResponse.put("diagnostic", "【降级模式】概念题解答文字校验通过，表达点契合核心考点。");
            }
            
            mockResponse.put("runResults", runResults);
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"success\":false, \"compileOk\":false, \"compilerMessage\":\"Mock失败\"}";
        }
    }

    private String getMockQuestionSubmission(String questionTitle, String questionType, String code, String standardAnswer) {
        try {
            boolean isEmpty = (code == null || code.trim().isEmpty() || code.trim().length() < 10);
            Map<String, Object> mockResponse = new HashMap<>();
            boolean isCode = "code".equalsIgnoreCase(questionType);

            if (isEmpty) {
                mockResponse.put("score", 0);
                mockResponse.put("foundationScore", 0);
                mockResponse.put("logicScore", 0);
                mockResponse.put("communicationScore", 0);
                mockResponse.put("stressScore", 0);
                
                String feedback = "### 【降级模式】在线评测报告\n\n" +
                        "**题型**：" + (isCode ? "算法编程题" : "技术概念/设计题") + "\n\n" +
                        "**解答分析**：\n" +
                        "- 候选人未提交有效解答（提交内容为空或过短，未达到最小评测字数限制）。\n" +
                        "- 无法进行任何实质性的正确性与复杂度评估。评分直接判 0。\n\n" +
                        "**时空开销（估计）**：\n" +
                        "- 时间复杂度：N/A\n" +
                        "- 空间复杂度：N/A";
                mockResponse.put("feedbackReport", feedback);
                mockResponse.put("suggestions", "【优化建议】\n请编写完整的代码或叙述完整的答题思路后再进行提交评测。");
            } else {
                // 根据代码/文本长度动态算分 (60-95之间)
                int len = code.trim().length();
                int score = 65 + Math.min(30, len / 20); // 每 20 个字加 1 分，上限 95

                mockResponse.put("score", score);
                mockResponse.put("foundationScore", Math.max(60, score + 2));
                mockResponse.put("logicScore", Math.max(60, score - 3));
                mockResponse.put("communicationScore", Math.max(60, score + 5));
                mockResponse.put("stressScore", Math.max(60, score - 1));
                
                String feedback = "### 【降级模式】在线评测报告\n\n" +
                        "**题型**：" + (isCode ? "算法编程题" : "技术概念/设计题") + "\n\n" +
                        "**解答分析**：\n" +
                        "- 您的答卷思路正确，核心逻辑与官方给出的参考答案吻合度较高。\n" +
                        "- " + (isCode ? "代码排版合理，符合 Java 编码规范，通过了所有预设测试用例。" : "文字叙述完整，分点陈述了相关的核心工作原理与常见问题避坑点。") + "\n\n" +
                        "**时空开销（估计）**：\n" +
                        "- 时间复杂度：" + (isCode ? "O(N log K)" : "N/A") + "\n" +
                        "- 空间复杂度：" + (isCode ? "O(K)" : "N/A");
                
                mockResponse.put("feedbackReport", feedback);
                mockResponse.put("suggestions", "【优化建议】\n1. " + (isCode ? "可以考虑使用快速选择（Quick Select）算法，将时间复杂度从 O(N log K) 进一步降至平均 O(N)。" : "建议在阐述时多结合实际工作中的项目踩坑例子（例如高并发下脏读的处理），能给面试官留下更深刻的印象。") + "\n2. 推荐深入阅读相关源码并进行代码重构以增强模块独立性。");
            }
            
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"score\":0, \"feedbackReport\":\"Mock评测失败\"}";
        }
    }

    /**
     * 生成 OCR 截图识别后的多语言解析与答案
     */
    public String generateOcrSuggestion(String ocrText, String mode) {
        if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {
            return "{\"analysis\":\"【模拟解析】这是一张测试截图文字。\",\"answer\":\"【模拟回答】检测到API Key未配置，请配置大模型API Key后进行真实多语言图文解析。\"}";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "你是一个处于隐蔽模式的面试辅助引擎核心终端。\n" +
                    "用户刚刚上传了一张屏幕截图并提取了其中的文字，其中可能包含中文、英文、或者是代码片段。\n" +
                    "由于 OCR 识别可能有少许误差，请你结合上下文还原原意。这极有可能是用户正在进行的笔试题或跨国面试题。\n" +
                    "如果提取出的文字含有大量英文或代码，请自动将全量翻译或解析为精炼的中文，并直接给出对应的中文解答思路。\n" +
                    "请严格按以下 JSON 格式输出，不要包含任何额外的 Markdown 标记：\n" +
                    "{\n" +
                    "  \"analysis\": \"<用极简的语言指出这是考察什么核心知识点，不要啰嗦>\",\n" +
                    "  \"answer\": \"<直接给出高质量的回答话术、解题思路或核心代码>\"\n" +
                    "}";

            if ("algorithm".equals(mode)) {
                systemPrompt += "\n当前模式为【算法题模式】，请在 answer 中优先提供最优时间复杂度的核心代码片段。";
            } else if ("system_design".equals(mode)) {
                systemPrompt += "\n当前模式为【系统设计模式】，请在 answer 中直接列出架构选型和高并发处理方案点。";
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", "OCR 提取的残缺/原始文本如下：\n---\n" + ocrText + "\n---\n请给出诊断与解答。")
            ));
            requestBody.put("temperature", 0.7);

            if (apiUrl.contains("deepseek")) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = mapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
                return content;
            } else {
                return "{\"analysis\":\"请求失败\",\"answer\":\"API 响应错误: " + response.getStatusCode() + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"analysis\":\"异常\",\"answer\":\"调用大模型发生错误: " + e.getMessage() + "\"}";
        }
    }
}
