package com.interviewai.controller;

import com.interviewai.entity.User;
import com.interviewai.service.LlmService;
import com.interviewai.service.UserService;
import com.interviewai.util.ResumeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private UserService userService;

    @Autowired
    private LlmService llmService;

    @GetMapping
    public String showResumePage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("username", user.getUsername());
        }
        return "resume";
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeResume(
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam(value = "targetJd", required = false) String targetJd) {

        Map<String, Object> response = new HashMap<>();
        try {
            String finalResumeText = "";

            // 优先解析上传的简历文件，若没有则取纯文本输入
            if (resumeFile != null && !resumeFile.isEmpty()) {
                finalResumeText = ResumeParser.extractText(resumeFile);
            } else if (resumeText != null && !resumeText.trim().isEmpty()) {
                finalResumeText = resumeText;
            } else {
                response.put("success", false);
                response.put("error", "请上传简历文件（支持 .pdf, .docx, .txt, .md）或在下方粘贴您的个人经历。");
                return ResponseEntity.badRequest().body(response);
            }

            if (finalResumeText.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "提取到的简历文本为空，请检查上传的文件内容或重新粘贴。");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用大模型对简历进行打分诊断与JD匹配评估
            String analysisResultJson = llmService.analyzeResume(finalResumeText, targetJd);

            response.put("success", true);
            response.put("analysis", analysisResultJson);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Throwable e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "系统崩溃或解析组件缺失，故障原因: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/optimizeFull")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> optimizeFullResume(
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam(value = "targetJd", required = false) String targetJd,
            @RequestParam(value = "versionType", required = false, defaultValue = "standard") String versionType) {

        Map<String, Object> response = new HashMap<>();
        try {
            String finalResumeText = "";

            if (resumeFile != null && !resumeFile.isEmpty()) {
                finalResumeText = ResumeParser.extractText(resumeFile);
            } else if (resumeText != null && !resumeText.trim().isEmpty()) {
                finalResumeText = resumeText;
            } else {
                response.put("success", false);
                response.put("error", "未能获取到简历内容。");
                return ResponseEntity.badRequest().body(response);
            }

            if (finalResumeText.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "简历内容为空。");
                return ResponseEntity.badRequest().body(response);
            }

            // 调用大模型生成全篇 ATS 优化简历 (支持多版本)
            String markdownResume = llmService.optimizeFullResume(finalResumeText, targetJd, versionType);

            response.put("success", true);
            response.put("markdown", markdownResume);
            return ResponseEntity.ok(response);

        } catch (Throwable e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "全量精修过程发生系统故障: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
