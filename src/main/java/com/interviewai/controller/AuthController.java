package com.interviewai.controller;

import com.interviewai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // 对应 login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 对应 register.html
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model) {
        try {
            userService.register(username, password); // 调用 UserService 的 register 方法
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
