package com.sleekydz86.loginstudy.auth.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(nullable = false, length = 200)
	private String password;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(name = "account_non_locked", nullable = false)
	private boolean accountNonLocked = true;

	@Column(name = "tenant_id", nullable = false, length = 64)
	private String tenantId;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Column(nullable = false, length = 30)
	private String phone;

	@Column(name = "member_type", nullable = false, length = 30)
	private String memberType;

	@Column(name = "terms_accepted_at", nullable = false)
	private Instant termsAcceptedAt;

	@Column(name = "privacy_accepted_at", nullable = false)
	private Instant privacyAcceptedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<UserRole> roles = new HashSet<>();

	protected UserAccount() {
	}

	public UserAccount(String username, String password, String email, String tenantId) {
		this(username, password, email, tenantId, username, "-", "DEMO", Instant.now());
	}

	public UserAccount(
			String username,
			String password,
			String email,
			String tenantId,
			String displayName,
			String phone,
			String memberType,
			Instant acceptedAt) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.tenantId = tenantId;
		this.displayName = displayName;
		this.phone = phone;
		this.memberType = memberType;
		this.termsAcceptedAt = acceptedAt;
		this.privacyAcceptedAt = acceptedAt;
	}

	public void addRole(String role) {
		this.roles.add(new UserRole(this, role));
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPhone() {
		return phone;
	}

	public String getMemberType() {
		return memberType;
	}

	public Instant getTermsAcceptedAt() {
		return termsAcceptedAt;
	}

	public Instant getPrivacyAcceptedAt() {
		return privacyAcceptedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Set<UserRole> getRoles() {
		return roles;
	}
}
