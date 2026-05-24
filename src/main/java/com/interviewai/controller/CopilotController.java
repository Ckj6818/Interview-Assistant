package com.interviewai.controller;

import com.interviewai.service.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class CopilotController {

    @Autowired
    private LlmService llmService;

    /**
     * 渲染实时提词助手页面
     */
    @GetMapping("/copilot")
    public String copilotPage() {
        return "copilot";
    }

    /**
     * 接收面试官的语音文本，返回大模型的提词建议
     */
    @PostMapping("/api/copilot/suggest")
    @ResponseBody
    public ResponseEntity<?> getSuggestion(@RequestBody Map<String, Object> payload) {
        String interviewerText = (String) payload.get("text");
        String mode = (String) payload.get("mode");
        String resumeText = (String) payload.get("resumeText");
        String targetJd = (String) payload.get("targetJd");
        Boolean isUserSpeakingOpt = (Boolean) payload.get("isUserSpeaking");
        boolean isUserSpeaking = isUserSpeakingOpt != null ? isUserSpeakingOpt : false;
        
        if (interviewerText == null || interviewerText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文本不能为空"));
        }

        // 调用大模型生成简短的回答大纲 (返回JSON字符串)
        String jsonResult = llmService.generateCopilotSuggestion(interviewerText, mode, resumeText, targetJd, isUserSpeaking);

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(jsonResult, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            data.put("success", true);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "analysis", "解析失败",
                    "answer", jsonResult
            ));
        }
    }

    /**
     * 接收截图文件进行 OCR 识别，并返回大模型解析结果
     */
    @PostMapping("/api/copilot/ocr-suggest")
    @ResponseBody
    public ResponseEntity<?> getOcrSuggestion(@org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                              @org.springframework.web.bind.annotation.RequestParam(value = "mode", defaultValue = "normal") String mode) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "未收到图片"));
        }

        try {
            // 将上传的文件保存为临时文件以供 Tess4J 读取
            java.io.File tempFile = java.io.File.createTempFile("ocr_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // TODO: Use OcrService
            com.interviewai.service.OcrService ocrService = new com.interviewai.service.OcrService();
            String ocrText = ocrService.extractTextFromImage(tempFile);
            
            // 清理临时文件
            tempFile.delete();

            if (ocrText == null || ocrText.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "error", "图片中未能识别出有效文字"));
            }

            // 限制过长的无用字符
            if (ocrText.length() > 3000) {
                ocrText = ocrText.substring(0, 3000);
            }

            // 发送给大模型进行解析
            String jsonResult = llmService.generateOcrSuggestion(ocrText, mode);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(jsonResult, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            data.put("success", true);
            data.put("ocrText", ocrText); // 返回提取的原始文本供前端校验
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("success", false, "error", "OCR处理异常: " + e.getMessage()));
        }
    }
}
