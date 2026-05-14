package com.esun.eCommerce_backend.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.esun.eCommerce_backend.dto.AdjustProductQuantityRequest;
import com.esun.eCommerce_backend.dto.CreateProductRequest;
import com.esun.eCommerce_backend.dto.UpdateProductQuantityRequest;
import com.esun.eCommerce_backend.model.Product;
import com.esun.eCommerce_backend.repository.ProductRepository;
import com.esun.eCommerce_backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;
	private final ProductService productService;

	public ProductController(ProductRepository productRepository, ProductService productService) {
		this.productRepository = productRepository;
		this.productService = productService;
	}

	@GetMapping
	public List<Product> getProducts() {
		return productRepository.findAll();
	}

	@GetMapping("/{productId}")
	public ResponseEntity<Product> getProduct(@PathVariable String productId) {
		return productRepository.findById(productId)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
		Product savedProduct = productService.createProduct(request);
		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{productId}")
			.buildAndExpand(savedProduct.getProductId())
			.toUri();
		return ResponseEntity.created(location).body(savedProduct);
	}

	@PutMapping("/{productId}/quantity")
	public ResponseEntity<Product> updateProductQuantity(@PathVariable String productId,
			@Valid @RequestBody UpdateProductQuantityRequest request) {
		return ResponseEntity.ok(productService.updateProductQuantity(productId, request));
	}

	@PostMapping("/{productId}/quantity-adjustments")
	public ResponseEntity<Product> adjustProductQuantity(@PathVariable String productId,
			@Valid @RequestBody AdjustProductQuantityRequest request) {
		return ResponseEntity.ok(productService.adjustProductQuantity(productId, request));
	}
}
