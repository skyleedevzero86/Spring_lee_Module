package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 200)
	private String manufacturer;

	@Column(name = "country_of_origin", length = 100)
	private String countryOfOrigin;

	@Column(name = "classification_code", length = 50)
	private String classificationCode;

	@Column(nullable = false)
	private boolean available;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected SupplierEntity() {
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getCountryOfOrigin() {
		return countryOfOrigin;
	}

	public String getClassificationCode() {
		return classificationCode;
	}

	public boolean isAvailable() {
		return available;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
