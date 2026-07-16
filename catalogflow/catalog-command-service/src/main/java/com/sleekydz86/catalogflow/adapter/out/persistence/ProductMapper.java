package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductImageEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductKeywordEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductTagEntity;
import com.sleekydz86.catalogflow.domain.model.AiEnrichmentStatus;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.ImageReference;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductKeyword;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.ProductStatus;
import com.sleekydz86.catalogflow.domain.model.ProductTag;
import com.sleekydz86.catalogflow.domain.model.SupplierId;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ProductMapper {

	private ProductMapper() {
	}

	public static Product toDomain(ProductEntity entity) {
		return Product.reconstitute(
				new ProductId(entity.getId()),
				new ProductName(entity.getName()),
				new ProductDescription(entity.getDescription()),
				new Money(entity.getPriceAmount(), entity.getPriceCurrency()),
				ProductStatus.valueOf(entity.getStatus()),
				new CategoryId(entity.getCategoryId()),
				new SupplierId(entity.getSupplierId()),
				entity.getImages().stream().map(ProductMapper::toImageReference).toList(),
				AiEnrichmentStatus.valueOf(entity.getAiEnrichmentStatus()),
				entity.getKeywords().stream()
						.map(keywordEntity -> new ProductKeyword(keywordEntity.getKeyword()))
						.collect(Collectors.toCollection(LinkedHashSet::new)),
				entity.getTags().stream()
						.map(tagEntity -> new ProductTag(tagEntity.getTag()))
						.collect(Collectors.toCollection(LinkedHashSet::new)),
				entity.getVersion(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getPublishedAt());
	}

	public static ProductEntity toNewEntity(Product product) {
		ProductEntity entity = ProductEntity.createEmpty();
		entity.setId(product.getId().value());
		applyScalars(product, entity);
		entity.setDeleted(false);
		entity.setCreatedAt(product.getCreatedAt());
		entity.setUpdatedAt(product.getUpdatedAt());
		syncChildren(product, entity);
		return entity;
	}

	public static void applyChanges(Product product, ProductEntity entity) {
		applyScalars(product, entity);
		syncChildren(product, entity);
	}

	private static void applyScalars(Product product, ProductEntity entity) {
		entity.setName(product.getName().value());
		entity.setDescription(product.getDescription().value());
		entity.setPriceAmount(product.getPrice().amount());
		entity.setPriceCurrency(product.getPrice().currency());
		entity.setStatus(product.getStatus().name());
		entity.setCategoryId(product.getCategoryId().value());
		entity.setSupplierId(product.getSupplierId().value());
		entity.setAiEnrichmentStatus(product.getAiEnrichmentStatus().name());
		entity.setPublishedAt(product.getPublishedAt());
		entity.setUpdatedAt(product.getUpdatedAt());
	}

	private static void syncChildren(Product product, ProductEntity entity) {
		entity.getImages().clear();
		for (ImageReference imageReference : product.getImages()) {
			ProductImageEntity imageEntity = ProductImageEntity.createEmpty();
			imageEntity.setId(UUID.randomUUID());
			imageEntity.setProduct(entity);
			imageEntity.setImageId(imageReference.imageId());
			imageEntity.setStorageKey(imageReference.storageKey());
			imageEntity.setContentType(imageReference.contentType());
			imageEntity.setSizeInBytes(imageReference.sizeInBytes());
			imageEntity.setTemporary(imageReference.temporary());
			imageEntity.setUploadedAt(imageReference.uploadedAt());
			entity.getImages().add(imageEntity);
		}

		entity.getKeywords().clear();
		for (ProductKeyword keyword : product.getKeywords()) {
			ProductKeywordEntity keywordEntity = ProductKeywordEntity.createEmpty();
			keywordEntity.setProduct(entity);
			keywordEntity.setKeyword(keyword.value());
			entity.getKeywords().add(keywordEntity);
		}

		entity.getTags().clear();
		for (ProductTag tag : product.getTags()) {
			ProductTagEntity tagEntity = ProductTagEntity.createEmpty();
			tagEntity.setProduct(entity);
			tagEntity.setTag(tag.value());
			entity.getTags().add(tagEntity);
		}
	}

	private static ImageReference toImageReference(ProductImageEntity entity) {
		return new ImageReference(
				entity.getImageId(),
				entity.getStorageKey(),
				entity.getContentType(),
				entity.getSizeInBytes(),
				entity.isTemporary(),
				entity.getUploadedAt());
	}
}
