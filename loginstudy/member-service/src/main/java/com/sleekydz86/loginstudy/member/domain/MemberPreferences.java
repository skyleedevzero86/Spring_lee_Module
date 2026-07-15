package com.sleekydz86.loginstudy.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_preferences")
public class MemberPreferences {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private MemberProfile member;

	@Column(name = "marketing_opt_in", nullable = false)
	private boolean marketingOptIn;

	@Column(nullable = false, length = 16)
	private String locale;

	@Column(nullable = false, length = 64)
	private String timezone;

	protected MemberPreferences() {
	}

	public MemberPreferences(boolean marketingOptIn, String locale, String timezone) {
		this.marketingOptIn = marketingOptIn;
		this.locale = locale;
		this.timezone = timezone;
	}

	void setMember(MemberProfile member) {
		this.member = member;
	}

	public void update(boolean marketingOptIn, String locale, String timezone) {
		this.marketingOptIn = marketingOptIn;
		this.locale = locale;
		this.timezone = timezone;
	}

	public Long getId() {
		return id;
	}

	public boolean isMarketingOptIn() {
		return marketingOptIn;
	}

	public String getLocale() {
		return locale;
	}

	public String getTimezone() {
		return timezone;
	}
}
