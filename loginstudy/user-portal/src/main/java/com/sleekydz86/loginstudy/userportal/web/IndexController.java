package com.sleekydz86.loginstudy.userportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "User Portal Pages", description = "OIDC 사용자 포털 화면")
public class IndexController {

	@GetMapping("/")
	@Operation(summary = "랜딩 페이지", description = "인증 없이 접근 가능")
	public String index(
			@CookieValue(name = "LOGIN_REMEMBER", defaultValue = "false") boolean rememberLogin,
			Model model) {
		model.addAttribute("rememberLogin", rememberLogin);
		return "index";
	}
}
