package com.sleekydz86.loginstudy.adminportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Admin Portal Pages", description = "관리자 OIDC 포털 화면")
public class IndexController {

	@GetMapping("/")
	@Operation(summary = "랜딩 페이지")
	public String index(
			@CookieValue(name = "LOGIN_REMEMBER", defaultValue = "false") boolean rememberLogin,
			Model model) {
		model.addAttribute("rememberLogin", rememberLogin);
		return "index";
	}

	@GetMapping("/access-denied")
	@Operation(summary = "권한 부족 안내")
	public String accessDenied() {
		return "access-denied";
	}
}
