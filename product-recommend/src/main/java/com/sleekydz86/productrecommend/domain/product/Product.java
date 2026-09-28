package com.sleekydz86.productrecommend.domain.product;

import java.util.List;
import java.util.Objects;

public final class Product {

	private final Long id;
	private final String name;
	private final List<String> keywords;
	private final float[] embedding;
	private final EmbeddingStatus embeddingStatus;
	private final String embeddingModel;

	private Product(
		Long id,
		String name,
		List<String> keywords,
		float[] embedding,
		EmbeddingStatus embeddingStatus,
		String embeddingModel
	) {
		this.id = id;
		this.name = requireText(name, "상품명");
		this.keywords = List.copyOf(keywords == null ? List.of() : keywords);
		this.embedding = embedding == null ? null : embedding.clone();
		this.embeddingStatus = embeddingStatus == null ? EmbeddingStatus.PENDING : embeddingStatus;
		this.embeddingModel = embeddingModel;
	}

	public static Product create(String name, List<String> keywords) {
		return new Product(null, name, keywords, null, EmbeddingStatus.PENDING, null);
	}

	public static Product rehydrate(
		Long id,
		String name,
		List<String> keywords,
		float[] embedding,
		EmbeddingStatus embeddingStatus,
		String embeddingModel
	) {
		return new Product(Objects.requireNonNull(id, "id"), name, keywords, embedding, embeddingStatus, embeddingModel);
	}

	public Product withEmbedding(float[] newEmbedding, String model, EmbeddingStatus status) {
		return new Product(id, name, keywords, newEmbedding, status, model);
	}

	public Product withNameAndKeywords(String newName, List<String> newKeywords) {
		return new Product(id, newName, newKeywords, null, EmbeddingStatus.PENDING, null);
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
}
