package com.sleekydz86.productrecommend.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Product {

	private final Long id;
	private final String name;
	private final List<String> keywords;
	private final String category;
	private final String brand;
	private final String color;
	private final BigDecimal price;
	private final int stock;
	private final ProductStatus status;
	private final double popularityScore;
	private final Instant createdAt;
	private final float[] embedding;
	private final EmbeddingStatus embeddingStatus;
	private final String embeddingModel;

	private Product(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		ProductStatus status,
		double popularityScore,
		Instant createdAt,
		float[] embedding,
		EmbeddingStatus embeddingStatus,
		String embeddingModel
	) {
		this.id = id;
		this.name = requireText(name, "상품명");
		this.keywords = List.copyOf(keywords == null ? List.of() : keywords);
		this.category = normalizeNullable(category);
		this.brand = normalizeNullable(brand);
		this.color = normalizeNullable(color);
		this.price = price == null ? BigDecimal.ZERO : price;
		this.stock = Math.max(0, stock);
		this.status = status == null ? ProductStatus.ACTIVE : status;
		this.popularityScore = Math.max(0, popularityScore);
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
		this.embedding = embedding == null ? null : embedding.clone();
		this.embeddingStatus = embeddingStatus == null ? EmbeddingStatus.PENDING : embeddingStatus;
		this.embeddingModel = embeddingModel;
	}

	public static Product create(
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock
	) {
		return new Product(
			null,
			name,
			keywords,
			category,
			brand,
			color,
			price,
			stock,
			ProductStatus.ACTIVE,
			0,
			Instant.now(),
			null,
			EmbeddingStatus.PENDING,
			null
		);
	}

	public static Product rehydrate(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		ProductStatus status,
		double popularityScore,
		Instant createdAt,
		float[] embedding,
		EmbeddingStatus embeddingStatus,
		String embeddingModel
	) {
		return new Product(
			Objects.requireNonNull(id, "id"),
			name,
			keywords,
			category,
			brand,
			color,
			price,
			stock,
			status,
			popularityScore,
			createdAt,
			embedding,
			embeddingStatus,
			embeddingModel
		);
	}

	public Product withEmbedding(float[] newEmbedding, String model, EmbeddingStatus status) {
		return new Product(
			id, name, keywords, category, brand, color, price, stock, this.status,
			popularityScore, createdAt, newEmbedding, status, model
		);
	}

	public Product withCatalog(
		String newName,
		List<String> newKeywords,
		String newCategory,
		String newBrand,
		String newColor,
		BigDecimal newPrice,
		int newStock,
		ProductStatus newStatus
	) {
		return new Product(
			id, newName, newKeywords, newCategory, newBrand, newColor, newPrice, newStock, newStatus,
			popularityScore, createdAt, null, EmbeddingStatus.PENDING, null
		);
	}

	public Product withPopularity(double score) {
		return new Product(
			id, name, keywords, category, brand, color, price, stock, status,
			score, createdAt, embedding, embeddingStatus, embeddingModel
		);
	}

	public boolean available() {
		return status == ProductStatus.ACTIVE && stock > 0;
	}

	public Long id() {
		return id;
	}

	public String name() {
		return name;
	}

	public List<String> keywords() {
		return keywords;
	}

	public String category() {
		return category;
	}

	public String brand() {
		return brand;
	}

	public String color() {
		return color;
	}

	public BigDecimal price() {
		return price;
	}

	public int stock() {
		return stock;
	}

	public ProductStatus status() {
		return status;
	}

	public double popularityScore() {
		return popularityScore;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public float[] embedding() {
		return embedding == null ? null : embedding.clone();
	}

	public EmbeddingStatus embeddingStatus() {
		return embeddingStatus;
	}

	public String embeddingModel() {
		return embeddingModel;
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + "은(는) 필수입니다");
		}
		return value.trim();
	}

	private static String normalizeNullable(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
