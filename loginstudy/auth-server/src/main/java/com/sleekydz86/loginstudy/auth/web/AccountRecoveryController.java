package com.sleekydz86.loginstudy.auth.web;

import com.sleekydz86.loginstudy.auth.service.AccountRecoveryService;
import com.sleekydz86.loginstudy.auth.web.AccountRecoveryForms.IdLookup;
import com.sleekydz86.loginstudy.auth.web.AccountRecoveryForms.PasswordReset;
import com.sleekydz86.loginstudy.auth.web.AccountRecoveryForms.PasswordVerification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Tag(name = "Account Recovery UI", description = "아이디 찾기 및 비밀번호 재설정 UI")
public class AccountRecoveryController {

	private static final String RESET_USERNAME = "passwordResetUsername";
	private static final String RESET_VERIFIED_AT = "passwordResetVerifiedAt";
	private static final Duration RESET_WINDOW = Duration.ofMinutes(10);

	private final AccountRecoveryService accountRecoveryService;

	public AccountRecoveryController(AccountRecoveryService accountRecoveryService) {
		this.accountRecoveryService = accountRecoveryService;
	}

	@GetMapping("/account-recovery/id")
	@Operation(summary = "아이디 찾기 페이지")
	public String idLookup(Model model) {
		model.addAttribute("idLookupForm", new IdLookup());
		return "find-id";
	}

	@PostMapping("/account-recovery/id")
	@Operation(summary = "이름과 휴대전화번호로 아이디 찾기")
	public String findId(
			@Valid @ModelAttribute("idLookupForm") IdLookup form,
			BindingResult bindingResult,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "find-id";
		}

		List<String> usernames = accountRecoveryService.findUsernames(form.getDisplayName(), form.getPhone());
		model.addAttribute("searched", true);
		model.addAttribute("usernames", usernames);
		return "find-id";
	}

	@GetMapping("/account-recovery/password")
	@Operation(summary = "비밀번호 찾기 페이지")
	public String passwordLookup(Model model) {
		model.addAttribute("passwordVerificationForm", new PasswordVerification());
		return "find-password";
	}

	@PostMapping("/account-recovery/password/verify")
	@Operation(summary = "비밀번호 재설정 본인정보 확인")
	public String verifyPasswordIdentity(
			@Valid @ModelAttribute("passwordVerificationForm") PasswordVerification form,
			BindingResult bindingResult,
			HttpSession session,
			Model model) {
		if (bindingResult.hasErrors()) {
			return "find-password";
		}

		if (!accountRecoveryService.verifyPasswordResetIdentity(
				form.getUsername(),
				form.getEmail(),
				form.getPhone())) {
			model.addAttribute("recoveryError", "입력한 정보와 일치하는 계정을 확인할 수 없습니다.");
			return "find-password";
		}

		session.setAttribute(RESET_USERNAME, form.getUsername().trim().toLowerCase(Locale.ROOT));
		session.setAttribute(RESET_VERIFIED_AT, Instant.now().toEpochMilli());
		return "redirect:/account-recovery/password/reset";
	}

	@GetMapping("/account-recovery/password/reset")
	@Operation(summary = "새 비밀번호 입력 페이지")
	public String passwordReset(HttpSession session, Model model) {
		if (verifiedUsername(session) == null) {
			return "redirect:/account-recovery/password";
		}
		model.addAttribute("passwordResetForm", new PasswordReset());
		return "reset-password";
	}

	@PostMapping("/account-recovery/password/reset")
	@Operation(summary = "새 비밀번호 저장")
	public String resetPassword(
			@Valid @ModelAttribute("passwordResetForm") PasswordReset form,
			BindingResult bindingResult,
			HttpSession session) {
		String username = verifiedUsername(session);
		if (username == null) {
			return "redirect:/account-recovery/password";
		}

		if (form.getPassword() != null && !form.getPassword().equals(form.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
		}
		if (bindingResult.hasErrors()) {
			return "reset-password";
		}

		accountRecoveryService.resetPassword(username, form.getPassword());
		clearResetVerification(session);
		return "redirect:/login?passwordReset";
	}

	private String verifiedUsername(HttpSession session) {
		Object username = session.getAttribute(RESET_USERNAME);
		Object verifiedAt = session.getAttribute(RESET_VERIFIED_AT);
		if (!(username instanceof String value) || !(verifiedAt instanceof Number timestamp)) {
			clearResetVerification(session);
			return null;
		}
		if (Instant.ofEpochMilli(timestamp.longValue()).plus(RESET_WINDOW).isBefore(Instant.now())) {
			clearResetVerification(session);
			return null;
		}
		return value;
	}

	private void clearResetVerification(HttpSession session) {
		session.removeAttribute(RESET_USERNAME);
		session.removeAttribute(RESET_VERIFIED_AT);
	}
}
