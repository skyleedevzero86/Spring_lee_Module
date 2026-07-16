package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;

final class ProductViewMapper {

	private ProductViewMapper() {
	}

	static ProductView toDomain(ProductViewDocument document) {
		ProductView view = ProductView.create(UUID.fromString(document.getProductId()));
		view.setName(document.getName());
		view.setSummary(document.getSummary());
		view.setDescription(document.getDescription());
		view.setPrice(document.getPrice());
		view.setCurrency(document.getCurrency());
		view.setStatus(document.getStatus());
		view.setCategoryId(document.getCategoryId() == null ? null : UUID.fromString(document.getCategoryId()));
		view.setSupplierId(document.getSupplierId() == null ? null : UUID.fromString(document.getSupplierId()));
		view.setSupplierName(document.getSupplierName());
		view.setImageUrls(document.getImageUrls());
		view.setKeywords(document.getKeywords());
		view.setTags(document.getTags());
		view.setAiGenerated(document.isAiGenerated());
		view.setAiModel(document.getAiModel());
		view.setPublishedAt(document.getPublishedAt());
		view.setCreatedAt(document.getCreatedAt());
		view.setUpdatedAt(document.getUpdatedAt());
		view.setVersion(document.getVersion());
		return view;
	}

	static ProductViewDocument toDocument(ProductView view) {
		ProductViewDocument document = ProductViewDocument.createEmpty(view.getProductId());
		document.setName(view.getName());
		document.setSummary(view.getSummary());
		document.setDescription(view.getDescription());
		document.setPrice(view.getPrice());
		document.setCurrency(view.getCurrency());
		document.setStatus(view.getStatus());
		document.setCategoryId(view.getCategoryId() == null ? null : view.getCategoryId().toString());
		document.setSupplierId(view.getSupplierId() == null ? null : view.getSupplierId().toString());
		document.setSupplierName(view.getSupplierName());
		document.setImageUrls(view.getImageUrls());
		document.setKeywords(view.getKeywords());
		document.setTags(view.getTags());
		document.setAiGenerated(view.isAiGenerated());
		document.setAiModel(view.getAiModel());
		document.setPublishedAt(view.getPublishedAt());
		document.setCreatedAt(view.getCreatedAt());
		document.setUpdatedAt(view.getUpdatedAt());
		document.setVersion(view.getVersion());
		return document;
	}
}
