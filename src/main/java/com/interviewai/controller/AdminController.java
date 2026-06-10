package com.interviewai.controller;

import com.interviewai.entity.Question;
import com.interviewai.service.QuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Tag(name = "Admin", description = "题库管理（管理员）")
public class AdminController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "admin/access-denied";
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalCount", questionService.getTotalQuestionCount());
        model.addAttribute("categories", questionService.getDistinctCategories());
        return "admin/index";
    }

    @GetMapping("/questions")
    public String listQuestions(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "difficulty", required = false) String difficulty,
            Model model) {
        List<Question> questions = questionService.searchForAdmin(category, keyword, difficulty);
        model.addAttribute("questions", questions);
        model.addAttribute("totalCount", questionService.getTotalQuestionCount());
        model.addAttribute("filteredCount", questions.size());
        model.addAttribute("categories", questionService.getDistinctCategories());
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("difficulty", difficulty);
        return "admin/questions";
    }

    @GetMapping("/questions/{id}")
    public String viewQuestion(@PathVariable Long id, Model model) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID: " + id));
        model.addAttribute("question", question);
        return "admin/question-detail";
    }

    @GetMapping("/questions/new")
    public String newQuestionForm(Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", questionService.getDistinctCategories());
        return "admin/question-form";
    }

    @GetMapping("/questions/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("无效的题目ID: " + id));
        model.addAttribute("question", question);
        model.addAttribute("isEdit", true);
        model.addAttribute("categories", questionService.getDistinctCategories());
        return "admin/question-form";
    }

    @PostMapping("/questions")
    public String saveQuestion(@ModelAttribute Question question, RedirectAttributes redirectAttributes) {
        boolean isNew = question.getId() == null;
        questionService.save(question);
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "题目新增成功" : "题目更新成功（ID: " + question.getId() + "）");
        return "redirect:/admin/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "题目已删除（ID: " + id + "）");
        return "redirect:/admin/questions";
    }
}
