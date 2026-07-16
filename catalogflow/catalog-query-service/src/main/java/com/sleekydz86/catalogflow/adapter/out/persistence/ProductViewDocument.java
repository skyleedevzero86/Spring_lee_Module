package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_views")
@CompoundIndex(name = "idx_status_published_at", def = "{'status': 1, 'publishedAt': -1}")
@CompoundIndex(name = "idx_category_status_published_at", def = "{'categoryId': 1, 'status': 1, 'publishedAt': -1}")
public class ProductViewDocument {

	@Id
	private String productId;
	@Indexed
	private String name;
	private String summary;
	private String description;
	private BigDecimal price;
	private String currency;
	@Indexed
	private String status;
	@Indexed
	private String categoryId;
	private String supplierId;
	private String supplierName;
	private List<String> imageUrls;
	@Indexed
	private List<String> keywords;
	private List<String> tags;
	private boolean aiGenerated;
	private String aiModel;
	@Indexed
	private Instant publishedAt;
	private Instant createdAt;
	private Instant updatedAt;
	private long version;

	public static ProductViewDocument createEmpty(UUID productId) {
		ProductViewDocument document = new ProductViewDocument();
		document.productId = productId.toString();
		document.imageUrls = new ArrayList<>();
		document.keywords = new ArrayList<>();
		document.tags = new ArrayList<>();
		return document;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
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

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
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
		this.imageUrls = imageUrls;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
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
