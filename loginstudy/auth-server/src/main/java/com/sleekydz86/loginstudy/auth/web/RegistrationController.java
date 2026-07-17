package com.sleekydz86.loginstudy.auth.web;

import com.sleekydz86.loginstudy.auth.service.RegistrationService;
import com.sleekydz86.loginstudy.auth.service.RegistrationService.DuplicateRegistrationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Tag(name = "Registration UI", description = "회원가입 약관 동의 및 계정 생성 UI")
public class RegistrationController {

	private final RegistrationService registrationService;

	public RegistrationController(RegistrationService registrationService) {
		this.registrationService = registrationService;
	}

	@GetMapping("/register")
	@Operation(summary = "회원가입 약관 동의 페이지")
	public String terms() {
		return "register-terms";
	}

	@PostMapping("/register/terms")
	@Operation(summary = "필수 약관 동의 확인")
	public String acceptTerms(
			@RequestParam(name = "termsAccepted", defaultValue = "false") boolean termsAccepted,
			@RequestParam(name = "privacyAccepted", defaultValue = "false") boolean privacyAccepted,
			Model model) {
		if (!termsAccepted || !privacyAccepted) {
			model.addAttribute("agreementError", "이용약관과 개인정보 처리방침에 모두 동의해야 합니다.");
			return "register-terms";
		}

		model.addAttribute("registrationForm", new RegistrationForm());
		return "register-details";
	}

	@PostMapping("/register")
	@Operation(summary = "일반 사용자 계정 생성")
	public String register(
			@Valid @ModelAttribute("registrationForm") RegistrationForm form,
			BindingResult bindingResult,
			@RequestParam(name = "termsAccepted", defaultValue = "false") boolean termsAccepted,
			@RequestParam(name = "privacyAccepted", defaultValue = "false") boolean privacyAccepted) {
		if (!termsAccepted || !privacyAccepted) {
			return "redirect:/register";
		}

		if (form.getPassword() != null && !form.getPassword().equals(form.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
		}
		if (bindingResult.hasErrors()) {
			return "register-details";
		}

		try {
			registrationService.register(form);
		}
		catch (DuplicateRegistrationException ex) {
			if ("username".equals(ex.getField())) {
				bindingResult.rejectValue("username", "duplicate", "이미 사용 중인 아이디입니다.");
			}
			else if ("email".equals(ex.getField())) {
				bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
			}
			else {
				bindingResult.reject("duplicate", "아이디 또는 이메일이 이미 사용 중입니다.");
			}
			return "register-details";
		}

		return "redirect:/register/complete";
	}

	@GetMapping("/register/complete")
	@Operation(summary = "회원가입 완료 페이지")
	public String complete() {
		return "register-complete";
	}
}
