package com.sleekydz86.loginstudy.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AccountRecoveryForms {

	private AccountRecoveryForms() {
	}

	public static class IdLookup {

		@NotBlank(message = "회원성명을 입력해 주세요.")
		@Size(max = 100, message = "회원성명은 100자 이하로 입력해 주세요.")
		private String displayName;

		@NotBlank(message = "휴대전화번호를 입력해 주세요.")
		@Pattern(regexp = "^[0-9-]{9,20}$", message = "휴대전화번호는 숫자와 하이픈만 입력해 주세요.")
		private String phone;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}
	}

	public static class PasswordVerification {

		@NotBlank(message = "아이디를 입력해 주세요.")
		@Size(max = 50, message = "아이디는 50자 이하로 입력해 주세요.")
		private String username;

		@NotBlank(message = "이메일을 입력해 주세요.")
		@Email(message = "올바른 이메일 형식으로 입력해 주세요.")
		@Size(max = 255, message = "이메일은 255자 이하로 입력해 주세요.")
		private String email;

		@NotBlank(message = "휴대전화번호를 입력해 주세요.")
		@Pattern(regexp = "^[0-9-]{9,20}$", message = "휴대전화번호는 숫자와 하이픈만 입력해 주세요.")
		private String phone;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}
	}

	public static class PasswordReset {

		@NotBlank(message = "새 비밀번호를 입력해 주세요.")
		@Size(min = 10, max = 72, message = "비밀번호는 10자 이상 72자 이하로 입력해 주세요.")
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
				message = "비밀번호에는 영문, 숫자, 특수문자가 각각 하나 이상 필요합니다.")
		private String password;

		@NotBlank(message = "새 비밀번호 확인을 입력해 주세요.")
		private String passwordConfirm;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getPasswordConfirm() {
			return passwordConfirm;
		}

		public void setPasswordConfirm(String passwordConfirm) {
			this.passwordConfirm = passwordConfirm;
		}
	}
}
