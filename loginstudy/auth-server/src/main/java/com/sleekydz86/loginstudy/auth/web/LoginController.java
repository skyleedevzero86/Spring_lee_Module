package com.sleekydz86.loginstudy.auth.web;

import com.sleekydz86.loginstudy.auth.security.LoginRememberSuccessHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Auth UI", description = "Authorization Server 로그인/동의 UI")
public class LoginController {

	@GetMapping("/login")
	@Operation(summary = "폼 로그인 페이지")
	public String login(
			@CookieValue(
					name = LoginRememberSuccessHandler.COOKIE_NAME,
					defaultValue = "false") boolean rememberLogin,
			Model model) {
		model.addAttribute("rememberLogin", rememberLogin);
		return "login";
	}
}
