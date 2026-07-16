package com.sleekydz86.catalogflow.application.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductView {

	private UUID productId;
	private String name;
	private String summary;
	private String description;
	private BigDecimal price;
	private String currency;
	private String status;
	private UUID categoryId;
	private UUID supplierId;
	private String supplierName;
	private List<String> imageUrls;
	private List<String> keywords;
	private List<String> tags;
	private boolean aiGenerated;
	private String aiModel;
	private Instant publishedAt;
	private Instant createdAt;
	private Instant updatedAt;
	private long version;

	public ProductView() {
		this.imageUrls = new ArrayList<>();
		this.keywords = new ArrayList<>();
		this.tags = new ArrayList<>();
	}

	public static ProductView create(UUID productId) {
		ProductView view = new ProductView();
		view.productId = productId;
		return view;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = new ArrayList<>(imageUrls);
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = new ArrayList<>(keywords);
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = new ArrayList<>(tags);
	}

	public boolean isAiGenerated() {
		return aiGenerated;
	}

	public void setAiGenerated(boolean aiGenerated) {
		this.aiGenerated = aiGenerated;
	}

	public String getAiModel() {
		return aiModel;
	}

	public void setAiModel(String aiModel) {
		this.aiModel = aiModel;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
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

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
