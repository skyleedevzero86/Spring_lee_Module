package com.sleekydz86.loginstudy.auth.security;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.domain.UserRole;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUser implements UserDetails {

	private final Long id;
	private final String username;
	private final String password;
	private final String email;
	private final String tenantId;
	private final boolean enabled;
	private final boolean accountNonLocked;
	private final Collection<? extends GrantedAuthority> authorities;

	public AuthUser(UserAccount account) {
		this.id = account.getId();
		this.username = account.getUsername();
		this.password = account.getPassword();
		this.email = account.getEmail();
		this.tenantId = account.getTenantId();
		this.enabled = account.isEnabled();
		this.accountNonLocked = account.isAccountNonLocked();
		this.authorities = account.getRoles().stream()
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

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
