package com.sleekydz86.searchai.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "model_price")
public class ModelPriceJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String model;

	@Column(nullable = false)
	private double inputPrice;

	@Column(nullable = false)
	private double outputPrice;

	@Column(nullable = false)
	private Instant effectiveFrom;

	private Instant effectiveTo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public double getInputPrice() {
		return inputPrice;
	}

	public void setInputPrice(double inputPrice) {
		this.inputPrice = inputPrice;
	}

	public double getOutputPrice() {
		return outputPrice;
	}

	public void setOutputPrice(double outputPrice) {
		this.outputPrice = outputPrice;
	}

	public Instant getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(Instant effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public Instant getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(Instant effectiveTo) {
		this.effectiveTo = effectiveTo;
	}
}
