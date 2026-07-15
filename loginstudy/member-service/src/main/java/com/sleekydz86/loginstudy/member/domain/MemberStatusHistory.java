package com.sleekydz86.loginstudy.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "member_status_history")
public class MemberStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberProfile member;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 32)
	private MemberStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 32)
	private MemberStatus toStatus;

	@Column(name = "changed_by", nullable = false, length = 100)
	private String changedBy;

	@Column(length = 255)
	private String reason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	protected MemberStatusHistory() {
	}

	public MemberStatusHistory(
			MemberProfile member,
			MemberStatus fromStatus,
			MemberStatus toStatus,
			String changedBy,
			String reason) {
		this.member = member;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.changedBy = changedBy;
		this.reason = reason;
	}

	public Long getId() {
		return id;
	}

	public MemberStatus getFromStatus() {
		return fromStatus;
	}

	public MemberStatus getToStatus() {
		return toStatus;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public String getReason() {
		return reason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
