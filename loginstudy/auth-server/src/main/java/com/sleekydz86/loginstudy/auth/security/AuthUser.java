package com.sleekydz86.loginstudy.auth.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.domain.UserRole;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthUser extends User {

	private final Long id;
	private final String email;
	private final String tenantId;

	public AuthUser(UserAccount account) {
		this(
				account.getUsername(),
				account.getPassword(),
				account.isEnabled(),
				true,
				true,
				account.isAccountNonLocked(),
				toAuthorities(account),
				account.getId(),
				account.getEmail(),
				account.getTenantId());
	}

	@JsonCreator
	public AuthUser(
			@JsonProperty("username") String username,
			@JsonProperty("password") String password,
			@JsonProperty("enabled") boolean enabled,
			@JsonProperty("accountNonExpired") boolean accountNonExpired,
			@JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
			@JsonProperty("accountNonLocked") boolean accountNonLocked,
			@JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
			@JsonProperty("id") Long id,
			@JsonProperty("email") String email,
			@JsonProperty("tenantId") String tenantId) {
		super(
				username,
				password == null ? "" : password,
				enabled,
				accountNonExpired,
				credentialsNonExpired,
				accountNonLocked,
				authorities);
		this.id = id;
		this.email = email;
		this.tenantId = tenantId;
	}

	private static Collection<? extends GrantedAuthority> toAuthorities(UserAccount account) {
		return account.getRoles().stream()
				.map(UserRole::getRole)
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toUnmodifiableSet());
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getTenantId() {
		return tenantId;
	}
}
