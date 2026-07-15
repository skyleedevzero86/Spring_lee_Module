package com.sleekydz86.loginstudy.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_roles")
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Id
	@Column(nullable = false, length = 50)
	private String role;

	protected UserRole() {
	}

	public UserRole(UserAccount user, String role) {
		this.user = user;
		this.role = role;
	}

	public UserAccount getUser() {
		return user;
	}

	public String getRole() {
		return role;
	}

	public static final class UserRoleId implements Serializable {

		private Long user;
		private String role;

		public UserRoleId() {
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof UserRoleId that)) {
				return false;
			}
			return Objects.equals(user, that.user) && Objects.equals(role, that.role);
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, role);
		}
	}
}
