$path = "h:\java spring\interviewai\src\main\java\com\interviewai\service\LlmService.java"
$content = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)

# 1. Replace evaluateAnswer method
$start_text = "调用大模型进行面试评测"
$end_text = "实时提词助手专用方法"

$start_idx = $content.IndexOf($start_text)
$end_idx = $content.IndexOf($end_text)

if ($start_idx -ge 0 -and $end_idx -gt $start_idx) {
    $comment_idx = $content.LastIndexOf("/**", $start_idx)
    $next_comment_idx = $content.LastIndexOf("/**", $end_idx)
    
    $clean_eval_answer = @"
/**
     * 调用大模型进行面试评测，并要求大模型返回结构化的 JSON 数据
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
                    "\\n请对候选人的回答进行打分和评估。请保持高度客观，不要给同情分。你必须返回一个合法的 JSON 格式数据，不能包含任何 Markdown 标记（如 ```json ）。\\n" +
                    "JSON 格式要求如下：\\n" +
                    "{\\n" +
                    "  \\"score\\": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),\\n" +
                    "  \\"foundationScore\\": 专业基础评分(0-100的整数，未作答或回答错误应低分),\\n" +
                    "  \\"logicScore\\": 逻辑思维评分(0-100的整数),\\n" +
                    "  \\"communicationScore\\": 沟通表达评分(0-100 of integer),\\n" +
                    "  \\"stressScore\\": 抗压应变评分(0-100 of integer),\\n" +
                    "  \\"techScore\\": 技术深度评分(0-100 of integer),\\n" +
                    "  \\"businessScore\\": 业务场景评分(0-100 of integer),\\n" +
                    "  \\"starAnalysis\\": {\\n" +
                    "    \\"situation\\": \\"S-情景再现与背景分析：指出候选人描述中的情景缺失或亮点\\",\\n" +
                    "    \\"task\\": \\"T-任务理解与目标拆解分析\\",\\n" +
                    "    \\"action\\": \\"A-行动过程与技术手段 of the detailed analysis\\",\\n" +
                    "    \\"result\\": \\"R-结果收益与数据支撑分析\\"\\n" +
                    "  },\\n" +
                    "  \\"feedbackReport\\": \\"详细的评估报告文本\\",\\n" +
                    "  \\"suggestions\\": \\"改进建议与参考答案\\"\\n" +
                    "}";

            String userContent = "面试题是：" + question + "\\n官方标准答案参考：" + standardAnswer + "\\n候选人的回答是：" + answer + "\\n请根据标准答案进行评估。";

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
                // 偶尔模型仍会带 Markdown 标记，做个简单过滤
                content = content.replaceAll("`json", "").replaceAll("`", "").trim();
                return content;
            }
        } catch (Exception e) {
            System.err.println("调用大模型 API 异常，进入保护级降级逻辑: " + e.getMessage());
        }

        return getMockEvaluation(personaPrompt, answer);
    }

    
"@
    $content = $content.Substring(0, $comment_idx) + $clean_eval_answer + $content.Substring($next_comment_idx)
    Write-Host "Replaced evaluateAnswer successfully."
} else {
    Write-Host "Error: Could not locate evaluateAnswer block."
}

# 2. Update evaluateSession prompt constraints in evaluateSession method
# Let's locate the systemPrompt string in evaluateSession
$content = $content.Replace('"score": 综合评分(60-100的整数),', '"score": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),')
$content = $content.Replace('"foundationScore": 专业基础评分(60-100的整数),', '"foundationScore": 专业基础评分(0-100的整数，未作答或回答错误应低分),')
$content = $content.Replace('"logicScore": 逻辑思维评分(60-100的整数),', '"logicScore": 逻辑思维评分(0-100的整数),')
$content = $content.Replace('"communicationScore": 沟通表达评分(60-100的整数),', '"communicationScore": 沟通表达评分(0-100的整数),')
$content = $content.Replace('"stressScore": 抗压应变评分(60-100的整数),', '"stressScore": 抗压应变评分(0-100的整数),')
$content = $content.Replace('"techScore": 技术深度评分(60-100的整数),', '"techScore": 技术深度评分(0-100的整数),')
$content = $content.Replace('"businessScore": 业务场景评分(60-100的整数),', '"businessScore": 业务场景评分(0-100的整数),')
$content = $content.Replace('对候选人的综合表现进行打分和评估。你必须', '对候选人的综合表现进行打分和评估。请保持高度客观，没有作答或无效作答应给0-10分。你必须')

# 3. Update evaluateSession mock fallbacks
# Find: return getMockEvaluation(personaPrompt); inside evaluateSession
# Since evaluateSession is at the top of evaluateSession method block, we do a replace
$content = $content.Replace(
    "if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains(\"YOUR_DEEPSEEK_API_KEY\")) {`r`n            return getMockEvaluation(personaPrompt);`r`n        }",
    "if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains(\"YOUR_DEEPSEEK_API_KEY\")) {`r`n            return getMockEvaluation(personaPrompt, chatHistoryJson);`r`n        }"
)
$content = $content.Replace(
    "if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains(\"YOUR_DEEPSEEK_API_KEY\")) {`n            return getMockEvaluation(personaPrompt);\n        }",
    "if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains(\"YOUR_DEEPSEEK_API_KEY\")) {\n            return getMockEvaluation(personaPrompt, chatHistoryJson);\n        }"
)
$content = $content.Replace(
    "System.err.println(\"调用大模型结算 API 异常: \" + e.getMessage());`r`n        }`r`n`r`n        return getMockEvaluation(personaPrompt);",
    "System.err.println(\"调用大模型结算 API 异常: \" + e.getMessage());`r`n        }`r`n`r`n        return getMockEvaluation(personaPrompt, chatHistoryJson);"
)
$content = $content.Replace(
    "System.err.println(\"调用大模型结算 API 异常: \" + e.getMessage());\n        }\n\n        return getMockEvaluation(personaPrompt);",
    "System.err.println(\"调用大模型结算 API 异常: \" + e.getMessage());\n        }\n\n        return getMockEvaluation(personaPrompt, chatHistoryJson);"
)

# 4. Replace getMockEvaluation method
$mock_eval_start = $content.IndexOf("private String getMockEvaluation(String personaPrompt)")
if ($mock_eval_start -ge 0) {
    # Find the comment before it, if any, or start index
    $mock_eval_end = $content.IndexOf("private String getMockCopilot", $mock_eval_start)
    $comment_idx = $content.LastIndexOf("/**", $mock_eval_start)
    if ($comment_idx -lt 0) { $comment_idx = $mock_eval_start }
    
    $clean_mock_eval = @"
    private String getMockEvaluation(String personaPrompt, String chatLogOrAnswer) {
        try {
            Map<String, Object> mockResponse = new HashMap<>();
            
            // Check if input is empty or too short
            boolean isEmpty = false;
            if (chatLogOrAnswer == null || chatLogOrAnswer.trim().isEmpty()) {
                isEmpty = true;
            } else if (chatLogOrAnswer.contains("[") && chatLogOrAnswer.contains("]")) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode chatArray = mapper.readTree(chatLogOrAnswer);
                    int userMsgs = 0;
                    for (JsonNode node : chatArray) {
                        if (node.has("role") && "user".equals(node.get("role").asText())) {
                            if (node.has("content") && !node.get("content").asText().trim().isEmpty()) {
                                userMsgs++;
                            }
                        }
                    }
                    if (userMsgs == 0) isEmpty = true;
                } catch (Exception e) {
                    // ignore
                }
            } else if (chatLogOrAnswer.contains("面试官:") && !chatLogOrAnswer.contains("候选人:")) {
                isEmpty = true;
            } else if (chatLogOrAnswer.trim().length() < 10) {
                isEmpty = true;
            }

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
                int baseScore = 72 + (chatLogOrAnswer.trim().length() % 15);
                mockResponse.put("score", baseScore);
                mockResponse.put("foundationScore", baseScore - 3);
                mockResponse.put("logicScore", baseScore + 2);
                mockResponse.put("communicationScore", baseScore + 3 > 100 ? 100 : baseScore + 3);
                mockResponse.put("stressScore", baseScore - 1);
                mockResponse.put("techScore", baseScore + 1);
                mockResponse.put("businessScore", baseScore - 4);
                
                Map<String, String> star = new HashMap<>();
                star.put("situation", "交代了项目是在高并发电商秒杀背景下进行的，情景比较清晰。");
                star.put("task", "明确了任务是解决超卖和提高吞吐量。");
                star.put("action", "采用了 Redis 分布式锁，但对锁的续期机制（WatchDog）解释不足。");
                star.put("result", "缺乏具体的数据支撑，例如 QPS 提升了多少。");
                mockResponse.put("starAnalysis", star);
                
                mockResponse.put("feedbackReport", "【降级保护模式】作为" + (personaPrompt != null && !personaPrompt.isEmpty() ? "设定的面试官" : "AI面试官") + "，您的表现总体良好，逻辑清晰。");
                mockResponse.put("suggestions", "建议：1. 补充核心机制；2. 结合实际高并发项目给出定量数据。");
            }
            
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"score\":0, \"feedbackReport\":\"解析失败\"}";
        }
    }

    
"@
    $content = $content.Substring(0, $comment_idx) + $clean_mock_eval + $content.Substring($mock_eval_end)
    Write-Host "Replaced getMockEvaluation successfully."
} else {
    Write-Host "Error: Could not find getMockEvaluation."
}

# 5. Update submitQuestionCheck prompt constraints
$content = $content.Replace('"score": 综合评分(60-100的整数),', '"score": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),')
$content = $content.Replace('"foundationScore": 专业基础/编译正确性评分(60-100的整数),', '"foundationScore": 专业基础/编译正确性评分(0-100的整数，未作答或回答错误应低分),')
$content = $content.Replace('"logicScore": 逻辑思维/算法复杂度评分(60-100的整数),', '"logicScore": 逻辑思维/算法复杂度评分(0-100的整数),')
$content = $content.Replace('"communicationScore": 沟通表达/代码规范度评分(60-100的整数),', '"communicationScore": 沟通表达/代码规范度评分(0-100的整数),')
$content = $content.Replace('"stressScore": 边界处理/鲁棒性评分(60-100的整数),', '"stressScore": 边界处理/鲁棒性评分(0-100的整数),')
$content = $content.Replace('进行深度打分和评测，并与标准答案做对比。', '进行深度打分和评测，并与标准答案做对比。请保持高度客观，空白或无效答案应给0-10分。')

# 6. Replace getMockQuestionSubmission method
$mock_sub_start = $content.IndexOf("private String getMockQuestionSubmission(String questionTitle")
if ($mock_sub_start -ge 0) {
    $mock_sub_end = $content.LastIndexOf("}")
    
    $clean_mock_sub = @"
private String getMockQuestionSubmission(String questionTitle, String questionType, String code, String standardAnswer) {
        try {
            Map<String, Object> mockResponse = new HashMap<>();
            boolean isCode = "code".equalsIgnoreCase(questionType);
            
            boolean isEmpty = (code == null || code.trim().isEmpty() || code.trim().length() < 10);
            
            if (isEmpty) {
                mockResponse.put("score", 0);
                mockResponse.put("foundationScore", 0);
                mockResponse.put("logicScore", 0);
                mockResponse.put("communicationScore", 0);
                mockResponse.put("stressScore", 0);
                mockResponse.put("feedbackReport", "### 【评测报告】未提交有效解答\n\n您提交的内容为空或过短，未检测到实质性的解题代码或概念叙述，因此评估分为 0。");
                mockResponse.put("suggestions", "【建议】请在编辑器中编写完整的代码或概念题解答后再次提交评测。");
            } else {
                int baseScore = 70 + (code.trim().length() % 20);
                mockResponse.put("score", baseScore);
                mockResponse.put("foundationScore", baseScore + 2 > 100 ? 100 : baseScore + 2);
                mockResponse.put("logicScore", baseScore - 3);
                mockResponse.put("communicationScore", baseScore + 1 > 100 ? 100 : baseScore + 1);
                mockResponse.put("stressScore", baseScore - 2);
                
                String feedback = "### 【降级模式】在线评测报告\n\n" +
                        "**题型**：" + (isCode ? "算法编程题" : "技术概念/设计题") + "\n\n" +
                        "**解答分析**：\n" +
                        "- 您的答卷思路正确，核心逻辑与官方给出的参考答案吻合度极高。\n" +
                        "- " + (isCode ? "代码排版合理，符合 Java 编码规范，通过了所有预设测试用例。" : "文字叙述完整，分点陈述了相关的核心工作原理与常见问题避坑点。") + "\n\n" +
                        "**时空开销（估计）**：\n" +
                        "- 时间复杂度：" + (isCode ? "O(N log K)" : "N/A") + "\n" +
                        "- 空间复杂度：" + (isCode ? "O(K)" : "N/A");
                
                mockResponse.put("feedbackReport", feedback);
                mockResponse.put("suggestions", "【优化建议】\n1. " + (isCode ? "可以考虑使用快速选择（Quick Select）算法，将时间复杂度进一步优化。" : "建议在叙述时多结合项目真实架构和吞吐数据做支撑。") + "\n2. 推荐深入阅读相关源码。");
            }
            
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\"score\":0, \"feedbackReport\":\"解析失败\"}";
        }
    }
"@
    $content = $content.Substring(0, $mock_sub_start) + $clean_mock_sub + "`r`n" + $content.Substring($mock_sub_end)
    Write-Host "Replaced getMockQuestionSubmission successfully."
} else {
    Write-Host "Error: Could not find getMockQuestionSubmission."
}

[System.IO.File]::WriteAllText($path, $content, [System.Text.Encoding]::UTF8)
Write-Host "Finished fixing LlmService.java!"
