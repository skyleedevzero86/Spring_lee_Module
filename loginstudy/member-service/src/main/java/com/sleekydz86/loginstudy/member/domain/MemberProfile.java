package com.sleekydz86.loginstudy.member.domain;

import com.sleekydz86.loginstudy.member.security.EncryptedStringConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member_profile")
public class MemberProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_subject", nullable = false, unique = true, length = 100)
	private String userSubject;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(nullable = false, unique = true, columnDefinition = "TEXT")
	private String email;

	@Convert(converter = EncryptedStringConverter.class)
	@Column(name = "display_name", nullable = false, columnDefinition = "TEXT")
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MemberStatus status;

	@Column(name = "tenant_id", nullable = false, length = 64)
	private String tenantId;

	@Version
	@Column(nullable = false)
	private Long version;

	@Column(name = "joined_at", nullable = false)
	private Instant joinedAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private MemberAddress address;

	@OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private MemberPreferences preferences;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<MemberStatusHistory> statusHistories = new ArrayList<>();

	protected MemberProfile() {
	}

	public MemberProfile(String userSubject, String email, String displayName, MemberStatus status, String tenantId) {
		Instant now = Instant.now();
		this.userSubject = userSubject;
		this.email = email;
		this.displayName = displayName;
		this.status = status;
		this.tenantId = tenantId;
		this.joinedAt = now;
		this.updatedAt = now;
	}

	public void changeDisplayName(String displayName) {
		this.displayName = displayName;
		this.updatedAt = Instant.now();
	}

	public void changeStatus(MemberStatus newStatus, String changedBy, String reason) {
		MemberStatus previous = this.status;
		this.status = newStatus;
		this.updatedAt = Instant.now();
		this.statusHistories.add(new MemberStatusHistory(this, previous, newStatus, changedBy, reason));
	}

	public void attachAddress(MemberAddress address) {
		this.address = address;
		address.setMember(this);
	}

	public void attachPreferences(MemberPreferences preferences) {
		this.preferences = preferences;
		preferences.setMember(this);
	}

	public Long getId() {
		return id;
	}

	public String getUserSubject() {
		return userSubject;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public MemberStatus getStatus() {
		return status;
	}

	public String getTenantId() {
		return tenantId;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getJoinedAt() {
		return joinedAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public MemberAddress getAddress() {
		return address;
	}

	public MemberPreferences getPreferences() {
		return preferences;
	}

	public List<MemberStatusHistory> getStatusHistories() {
		return statusHistories;
	}
}
