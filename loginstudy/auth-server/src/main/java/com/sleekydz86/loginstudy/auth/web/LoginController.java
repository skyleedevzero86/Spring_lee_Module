package com.sleekydz86.loginstudy.auth.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Auth UI", description = "Authorization Server 로그인/동의 UI")
public class LoginController {

	@GetMapping("/login")
	@Operation(summary = "폼 로그인 페이지")
	public String login() {
		return "login";
	}
}
