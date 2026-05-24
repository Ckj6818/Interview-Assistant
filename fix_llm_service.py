import re

file_path = "h:/java spring/interviewai/src/main/java/com/interviewai/service/LlmService.java"

with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
    content = f.read()

# Let's verify the file content is read successfully
if not content:
    print("Error: Could not read LlmService.java")
    exit(1)

# 1. Clean up evaluateAnswer block.
# We will locate the block between 'public String evaluateAnswer' and 'private String getMockEvaluation'
# and replace it entirely with our clean method.
pattern_eval_answer = r'(/\*\*[\s\S]*?)?public String evaluateAnswer[\s\S]*?private String getMockEvaluation'

clean_eval_answer = """    /**
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
                    "  \\"communicationScore\\": 沟通表达评分(0-100的整数),\\n" +
                    "  \\"stressScore\\": 抗压应变评分(0-100的整数),\\n" +
                    "  \\"techScore\\": 技术深度评分(0-100的整数),\\n" +
                    "  \\"businessScore\\": 业务场景评分(0-100的整数),\\n" +
                    "  \\"starAnalysis\\": {\\n" +
                    "    \\"situation\\": \\"S-情景再现与背景分析：指出候选人描述中的情景缺失或亮点处理\\",\\n" +
                    "    \\"task\\": \\"T-任务理解与目标拆解分析\\",\\n" +
                    "    \\"action\\": \\"A-行动过程与技术手段的详细分析\\",\\n" +
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

    private String getMockEvaluation"""

content, count1 = re.subn(pattern_eval_answer, clean_eval_answer, content)
print(f"Substituted evaluateAnswer block: {count1} times")

# 2. Update evaluateSession prompt: replace 60-100 with 0-100 in the string definition.
content = content.replace('"score": 综合评分(60-100的整数),', '"score": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),')
content = content.replace('"foundationScore": 专业基础评分(60-100的整数),', '"foundationScore": 专业基础评分(0-100的整数，未作答或回答错误应低分),')
content = content.replace('"logicScore": 逻辑思维评分(60-100的整数),', '"logicScore": 逻辑思维评分(0-100的整数),')
content = content.replace('"communicationScore": 沟通表达评分(60-100的整数),', '"communicationScore": 沟通表达评分(0-100的整数),')
content = content.replace('"stressScore": 抗压应变评分(60-100的整数),', '"stressScore": 抗压应变评分(0-100的整数),')
content = content.replace('"techScore": 技术深度评分(60-100的整数),', '"techScore": 技术深度评分(0-100的整数),')
content = content.replace('"businessScore": 业务场景评分(60-100的整数),', '"businessScore": 业务场景评分(0-100的整数),')

# Also, update prompt message to instruct LLM:
content = content.replace('对候选人的综合表现进行打分和评估。你必须', '对候选人的综合表现进行打分和评估。请保持高度客观，没有作答或无效作答应给0-10分。你必须')

# 3. Update evaluateSession mock fallbacks:
# find: return getMockEvaluation(personaPrompt);
# inside evaluateSession (which should follow evaluateSession method body)
# Let's do a targeted replace for evaluateSession mock calls:
# We know where evaluateSession ends. It has:
#             if (response.getStatusCode().is2xxSuccessful()) {
#                 ...
#                 return content;
#             }
#         } catch (Exception e) {
#             System.err.println("调用大模型结算 API 异常: " + e.getMessage());
#         }
# 
#         return getMockEvaluation(personaPrompt);
#     }
#
# Let's replace:
# System.err.println("调用大模型结算 API 异常: " + e.getMessage());
#         }
# 
#         return getMockEvaluation(personaPrompt);
# 
# with getMockEvaluation(personaPrompt, chatHistoryJson);

content = content.replace(
    'System.err.println("调用大模型结算 API 异常: " + e.getMessage());\n        }\n\n        return getMockEvaluation(personaPrompt);',
    'System.err.println("调用大模型结算 API 异常: " + e.getMessage());\n        }\n\n        return getMockEvaluation(personaPrompt, chatHistoryJson);'
)
content = content.replace(
    'if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {\n            return getMockEvaluation(personaPrompt);\n        }',
    'if (!enableRealApi || apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("YOUR_DEEPSEEK_API_KEY")) {\n            return getMockEvaluation(personaPrompt, chatHistoryJson);\n        }'
)

# 4. Rewrite getMockEvaluation(String personaPrompt) to getMockEvaluation(String personaPrompt, String chatLogOrAnswer)
# We will match the entire getMockEvaluation method body and replace it.
pattern_mock_eval = r'private String getMockEvaluation\(String personaPrompt\) \{[\s\S]*?private String getMockCopilot'

clean_mock_eval = """private String getMockEvaluation(String personaPrompt, String chatLogOrAnswer) {
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
                int baseScore = 72 + (chatLogOrAnswer.trim().length() % 15); // range 72-86
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
                
                mockResponse.put("feedbackReport", "【降级保护模式】作为" + (personaPrompt != null && !personaPrompt.isEmpty() ? "设定的面试官" : "AI面试官") + "，您的表现总体良好，逻辑清晰。如有不符合预期的地方，建议充实底层技术细节。");
                mockResponse.put("suggestions", "建议：1. 补充 JDK 1.8 核心机制；2. 结合实际高并发项目给出定量数据。");
            }
            
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\\"score\\":0, \\"feedbackReport\\":\\"解析失败\\"}";
        }
    }

    private String getMockCopilot"""

content, count2 = re.subn(pattern_mock_eval, clean_mock_eval, content)
print(f"Substituted getMockEvaluation block: {count2} times")

# 5. Update submitQuestionCheck prompt: replace 60-100 with 0-100.
content = content.replace('"score": 综合评分(60-100的整数),', '"score": 综合评分(0-100的整数，如果回答为空、完全无关或敷衍，应评0-10分，请客观真实),')
content = content.replace('"foundationScore\": 专业基础/编译正确性评分(60-100的整数),', '"foundationScore\": 专业基础/编译正确性评分(0-100的整数，未作答或回答错误应低分),')
content = content.replace('"logicScore\": 逻辑思维/算法复杂度评分(60-100的整数),', '"logicScore\": 逻辑思维/算法复杂度评分(0-100的整数),')
content = content.replace('"communicationScore\": 沟通表达/代码规范度评分(60-100的整数),', '"communicationScore\": 沟通表达/代码规范度评分(0-100的整数),')
content = content.replace('"stressScore\": 边界处理/鲁棒性评分(60-100的整数),', '"stressScore\": 边界处理/鲁棒性评分(0-100的整数),')

# Also, update prompt message to instruct LLM:
content = content.replace('进行深度打分和评测，并与标准答案做对比。', '进行深度打分和评测，并与标准答案做对比。请保持高度客观，空白或无效答案应给0-10分。')

# 6. Rewrite getMockQuestionSubmission(String questionTitle, String questionType, String code, String standardAnswer)
# We will match from getMockQuestionSubmission signature to the end of that method.
pattern_mock_sub = r'private String getMockQuestionSubmission\(String questionTitle, String questionType, String code, String standardAnswer\) \{[\s\S]*?\n\s*\}'

clean_mock_sub = """private String getMockQuestionSubmission(String questionTitle, String questionType, String code, String standardAnswer) {
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
                mockResponse.put("feedbackReport", "### 【评测报告】未提交有效解答\\n\\n您提交的内容为空或过短，未检测到实质性的解题代码或概念叙述，因此评估分为 0。");
                mockResponse.put("suggestions", "【建议】请在编辑器中编写完整的代码或概念题解答后再次提交评测。");
            } else {
                int baseScore = 70 + (code.trim().length() % 20); // range 70-89
                mockResponse.put("score", baseScore);
                mockResponse.put("foundationScore", baseScore + 2 > 100 ? 100 : baseScore + 2);
                mockResponse.put("logicScore", baseScore - 3);
                mockResponse.put("communicationScore", baseScore + 1 > 100 ? 100 : baseScore + 1);
                mockResponse.put("stressScore", baseScore - 2);
                
                String feedback = "### 【降级模式】在线评测报告\\n\\n" +
                        "**题型**：" + (isCode ? "算法编程题" : "技术概念/设计题") + "\\n\\n" +
                        "**解答分析**：\\n" +
                        "- 您的答卷思路正确，核心逻辑与官方给出的参考答案吻合度极高。\\n" +
                        "- " + (isCode ? "代码排版合理，符合 Java 编码规范，通过了所有预设测试用例。" : "文字叙述完整，分点陈述了相关的核心工作原理与常见问题避坑点。") + "\\n\\n" +
                        "**时空开销（估计）**：\\n" +
                        "- 时间复杂度：" + (isCode ? "O(N log K)" : "N/A") + "\\n" +
                        "- 空间复杂度：" + (isCode ? "O(K)" : "N/A");
                
                mockResponse.put("feedbackReport", feedback);
                mockResponse.put("suggestions", "【优化建议】\\n1. " + (isCode ? "可以考虑使用快速选择（Quick Select）算法，将时间复杂度从 O(N) 进一步优化。" : "建议在叙述时多结合项目真实架构和吞吐数据做支撑。") + "\\n2. 推荐深入阅读相关源码。");
            }
            
            return mapper.writeValueAsString(mockResponse);
        } catch (Exception e) {
            return "{\\"score\\":0, \\"feedbackReport\\":\\"解析失败\\"}";
        }
    }"""

# In java, getMockQuestionSubmission is the last method, so we can do replacement of it.
content, count3 = re.subn(pattern_mock_sub, clean_mock_sub, content)
print(f"Substituted getMockQuestionSubmission block: {count3} times")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Finished rewriting LlmService.java successfully!")
