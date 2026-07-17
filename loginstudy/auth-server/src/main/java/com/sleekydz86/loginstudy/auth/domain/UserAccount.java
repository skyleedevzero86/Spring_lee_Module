package com.sleekydz86.loginstudy.auth.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false, length = 20)
	private AccountStatus status = AccountStatus.ACTIVE;

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

	public void changeStatus(AccountStatus newStatus) {
		if (this.status == AccountStatus.DELETED && newStatus != AccountStatus.DELETED) {
			throw new IllegalArgumentException("삭제된 계정은 복구할 수 없습니다");
		}
		this.status = newStatus;
		this.enabled = newStatus == AccountStatus.ACTIVE || newStatus == AccountStatus.SUSPENDED;
		this.accountNonLocked = newStatus != AccountStatus.SUSPENDED;
	}

	public void replaceRole(String role) {
		if (!"USER".equals(role) && !"ADMIN".equals(role)) {
			throw new IllegalArgumentException("지원하지 않는 권한입니다: " + role);
		}
		this.roles.clear();
		addRole(role);
	}

	public void changeProfile(String displayName, String email, String phone) {
		this.displayName = displayName;
		this.email = email;
		this.phone = phone;
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

	public AccountStatus getStatus() {
		return status;
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
