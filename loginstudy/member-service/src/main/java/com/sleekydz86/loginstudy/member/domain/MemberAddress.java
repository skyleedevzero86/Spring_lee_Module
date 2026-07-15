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
@Table(name = "member_address")
public class MemberAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private MemberProfile member;

	@Column(name = "country_code", nullable = false, length = 2)
	private String countryCode;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(name = "street_line", nullable = false, length = 255)
	private String streetLine;

	@Column(name = "postal_code", nullable = false, length = 32)
	private String postalCode;

	protected MemberAddress() {
	}

	public MemberAddress(String countryCode, String city, String streetLine, String postalCode) {
		this.countryCode = countryCode;
		this.city = city;
		this.streetLine = streetLine;
		this.postalCode = postalCode;
	}

	void setMember(MemberProfile member) {
		this.member = member;
	}

	public void update(String countryCode, String city, String streetLine, String postalCode) {
		this.countryCode = countryCode;
		this.city = city;
		this.streetLine = streetLine;
		this.postalCode = postalCode;
	}

	public Long getId() {
		return id;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getCity() {
		return city;
	}

	public String getStreetLine() {
		return streetLine;
	}

	public String getPostalCode() {
		return postalCode;
	}
}
