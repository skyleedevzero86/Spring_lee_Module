package com.sleekydz86.loginstudy.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

	@NotBlank(message = "아이디를 입력해 주세요.")
	@Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이하로 입력해 주세요.")
	@Pattern(regexp = "^[a-z0-9_-]+$", message = "아이디는 영문 소문자, 숫자, 밑줄, 하이픈만 사용할 수 있습니다.")
	private String username;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Size(min = 10, max = 72, message = "비밀번호는 10자 이상 72자 이하로 입력해 주세요.")
	@Pattern(
			regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
			message = "비밀번호에는 영문, 숫자, 특수문자가 각각 하나 이상 필요합니다.")
	private String password;

	@NotBlank(message = "비밀번호 확인을 입력해 주세요.")
	private String passwordConfirm;

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일 형식으로 입력해 주세요.")
	@Size(max = 255, message = "이메일은 255자 이하로 입력해 주세요.")
	private String email;

	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(max = 100, message = "이름은 100자 이하로 입력해 주세요.")
	private String displayName;

	@NotBlank(message = "휴대전화번호를 입력해 주세요.")
	@Pattern(regexp = "^[0-9-]{9,20}$", message = "휴대전화번호는 숫자와 하이픈만 입력해 주세요.")
	private String phone;

	@NotBlank(message = "회원 구분을 선택해 주세요.")
	@Pattern(regexp = "PERSONAL|BUSINESS", message = "올바른 회원 구분을 선택해 주세요.")
	private String memberType;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

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

	public String getMemberType() {
		return memberType;
	}

	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}
}
