package com.interviewai.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = AdminController.class)
public class AdminControllerAdvice {

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        return authentication != null ? authentication.getName() : "";
    }
}
