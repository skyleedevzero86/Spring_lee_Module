package com.sleekydz86.sever.model.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "사용자명 또는 비밀번호가 올바르지 않습니다");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다");
        }
        return "login";
    }
}

