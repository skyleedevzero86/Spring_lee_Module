package com.sleekydz86.catalogflow.adapter.in.web;

import java.net.URI;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.in.web.dto.ChangeProductPriceRequest;
import com.sleekydz86.catalogflow.adapter.in.web.dto.CreateProductRequest;
import com.sleekydz86.catalogflow.adapter.in.web.dto.ProductResponse;
import com.sleekydz86.catalogflow.adapter.in.web.dto.RegisterProductImageRequest;
import com.sleekydz86.catalogflow.adapter.in.web.dto.UpdateProductRequest;
import com.sleekydz86.catalogflow.adapter.in.web.dto.VersionedActionRequest;
import com.sleekydz86.catalogflow.application.command.ChangeProductPriceCommand;
import com.sleekydz86.catalogflow.application.command.CreateProductCommand;
import com.sleekydz86.catalogflow.application.command.ProductLifecycleCommand;
import com.sleekydz86.catalogflow.application.command.RegisterProductImageCommand;
import com.sleekydz86.catalogflow.application.command.UpdateProductCommand;
import com.sleekydz86.catalogflow.application.port.in.ApproveAiEnrichmentUseCase;
import com.sleekydz86.catalogflow.application.port.in.ChangeProductPriceUseCase;
import com.sleekydz86.catalogflow.application.port.in.CreateProductUseCase;
import com.sleekydz86.catalogflow.application.port.in.PublishProductUseCase;
import com.sleekydz86.catalogflow.application.port.in.RegisterProductImageUseCase;
import com.sleekydz86.catalogflow.application.port.in.RequestAiEnrichmentUseCase;
import com.sleekydz86.catalogflow.application.port.in.SuspendProductUseCase;
import com.sleekydz86.catalogflow.application.port.in.UpdateProductUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/products")
public class ProductCommandController {

	private final CreateProductUseCase createProductUseCase;
	private final UpdateProductUseCase updateProductUseCase;
	private final ChangeProductPriceUseCase changeProductPriceUseCase;
	private final RegisterProductImageUseCase registerProductImageUseCase;
	private final RequestAiEnrichmentUseCase requestAiEnrichmentUseCase;
	private final ApproveAiEnrichmentUseCase approveAiEnrichmentUseCase;
	private final PublishProductUseCase publishProductUseCase;
	private final SuspendProductUseCase suspendProductUseCase;

	public ProductCommandController(
			CreateProductUseCase createProductUseCase,
			UpdateProductUseCase updateProductUseCase,
			ChangeProductPriceUseCase changeProductPriceUseCase,
			RegisterProductImageUseCase registerProductImageUseCase,
			RequestAiEnrichmentUseCase requestAiEnrichmentUseCase,
			ApproveAiEnrichmentUseCase approveAiEnrichmentUseCase,
			PublishProductUseCase publishProductUseCase,
			SuspendProductUseCase suspendProductUseCase) {
		this.createProductUseCase = createProductUseCase;
		this.updateProductUseCase = updateProductUseCase;
		this.changeProductPriceUseCase = changeProductPriceUseCase;
		this.registerProductImageUseCase = registerProductImageUseCase;
		this.requestAiEnrichmentUseCase = requestAiEnrichmentUseCase;
		this.approveAiEnrichmentUseCase = approveAiEnrichmentUseCase;
		this.publishProductUseCase = publishProductUseCase;
		this.suspendProductUseCase = suspendProductUseCase;
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
		var result = createProductUseCase.create(new CreateProductCommand(
				request.name(),
				request.description() == null ? "" : request.description(),
				request.priceAmount(),
				request.priceCurrency(),
				request.categoryId(),
				request.supplierId()));

		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{productId}")
				.buildAndExpand(result.productId())
				.toUri();
		return ResponseEntity.created(location).body(ProductResponse.from(result));
	}

	@PutMapping("/{productId}")
	public ProductResponse update(
			@PathVariable UUID productId,
			@Valid @RequestBody UpdateProductRequest request) {
		var result = updateProductUseCase.update(new UpdateProductCommand(
				productId,
				request.version(),
				request.name(),
				request.description() == null ? "" : request.description(),
				request.categoryId(),
				request.supplierId()));
		return ProductResponse.from(result);
	}

	@PatchMapping("/{productId}/price")
	public ProductResponse changePrice(
			@PathVariable UUID productId,
			@Valid @RequestBody ChangeProductPriceRequest request) {
		var result = changeProductPriceUseCase.changePrice(new ChangeProductPriceCommand(
				productId,
				request.version(),
				request.priceAmount(),
				request.priceCurrency()));
		return ProductResponse.from(result);
	}

	@PostMapping("/{productId}/images")
	public ProductResponse registerImage(
			@PathVariable UUID productId,
			@Valid @RequestBody RegisterProductImageRequest request) {
		var result = registerProductImageUseCase.registerImage(new RegisterProductImageCommand(
				productId,
				request.version(),
				request.storageKey(),
				request.contentType(),
				request.sizeInBytes(),
				request.temporary()));
		return ProductResponse.from(result);
	}

	@PostMapping("/{productId}/ai-enrichment")
	public ProductResponse requestAiEnrichment(
			@PathVariable UUID productId,
			@Valid @RequestBody VersionedActionRequest request) {
		var result = requestAiEnrichmentUseCase.request(productId, request.version());
		return ProductResponse.from(result);
	}

	@PostMapping("/{productId}/ai-enrichment/approve")
	public ProductResponse approveAiEnrichment(
			@PathVariable UUID productId,
			@Valid @RequestBody VersionedActionRequest request) {
		var result = approveAiEnrichmentUseCase.approve(productId, request.version());
		return ProductResponse.from(result);
	}

	@PostMapping("/{productId}/publish")
	public ProductResponse publish(
			@PathVariable UUID productId,
			@Valid @RequestBody VersionedActionRequest request) {
		var result = publishProductUseCase.publish(productId, request.version());
		return ProductResponse.from(result);
	}

	@PostMapping("/{productId}/suspend")
	public ProductResponse suspend(
			@PathVariable UUID productId,
			@Valid @RequestBody VersionedActionRequest request) {
		var result = suspendProductUseCase.suspend(new ProductLifecycleCommand(
				productId,
				request.version(),
				request.reason() == null ? "" : request.reason()));
		return ProductResponse.from(result);
	}
}
