package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_enrichment_results")
public class AiEnrichmentResultEntity {

	@Id
	private UUID id;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "model_name", nullable = false, length = 100)
	private String modelName;

	@Column(columnDefinition = "TEXT")
	private String summary;

	@Column(name = "generated_description", columnDefinition = "TEXT")
	private String generatedDescription;

	@Column(name = "recommended_category", length = 100)
	private String recommendedCategory;

	@Column(columnDefinition = "TEXT")
	private String warnings;

	@Column(name = "requires_human_review", nullable = false)
	private boolean requiresHumanReview;

	@Column(precision = 5, scale = 4)
	private BigDecimal confidence;

	@Column(name = "prompt_version", length = 50)
	private String promptVersion;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getGeneratedDescription() {
		return generatedDescription;
	}

	public void setGeneratedDescription(String generatedDescription) {
		this.generatedDescription = generatedDescription;
	}

	public String getRecommendedCategory() {
		return recommendedCategory;
	}

	public void setRecommendedCategory(String recommendedCategory) {
		this.recommendedCategory = recommendedCategory;
	}

	public String getWarnings() {
		return warnings;
	}

	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}

	public boolean isRequiresHumanReview() {
		return requiresHumanReview;
	}

	public void setRequiresHumanReview(boolean requiresHumanReview) {
		this.requiresHumanReview = requiresHumanReview;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}

	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}

	public String getPromptVersion() {
		return promptVersion;
	}

	public void setPromptVersion(String promptVersion) {
		this.promptVersion = promptVersion;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
