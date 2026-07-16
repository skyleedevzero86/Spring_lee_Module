package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
@SQLRestriction("deleted = false")
public class ProductEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal priceAmount;

	@Column(name = "price_currency", nullable = false, length = 3)
	private String priceCurrency;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "category_id", nullable = false)
	private UUID categoryId;

	@Column(name = "supplier_id", nullable = false)
	private UUID supplierId;

	@Column(name = "ai_enrichment_status", nullable = false, length = 30)
	private String aiEnrichmentStatus;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(nullable = false)
	private boolean deleted;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProductImageEntity> images = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProductKeywordEntity> keywords = new HashSet<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProductTagEntity> tags = new HashSet<>();

	protected ProductEntity() {
	}

	public static ProductEntity createEmpty() {
		return new ProductEntity();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPriceAmount() {
		return priceAmount;
	}

	public void setPriceAmount(BigDecimal priceAmount) {
		this.priceAmount = priceAmount;
	}

	public String getPriceCurrency() {
		return priceCurrency;
	}

	public void setPriceCurrency(String priceCurrency) {
		this.priceCurrency = priceCurrency;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UUID getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(UUID categoryId) {
		this.categoryId = categoryId;
	}

	public UUID getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(UUID supplierId) {
		this.supplierId = supplierId;
	}

	public String getAiEnrichmentStatus() {
		return aiEnrichmentStatus;
	}

	public void setAiEnrichmentStatus(String aiEnrichmentStatus) {
		this.aiEnrichmentStatus = aiEnrichmentStatus;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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

	public List<ProductImageEntity> getImages() {
		return images;
	}

	public void setImages(List<ProductImageEntity> images) {
		this.images = images;
	}

	public Set<ProductKeywordEntity> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<ProductKeywordEntity> keywords) {
		this.keywords = keywords;
	}

	public Set<ProductTagEntity> getTags() {
		return tags;
	}

	public void setTags(Set<ProductTagEntity> tags) {
		this.tags = tags;
	}
}
