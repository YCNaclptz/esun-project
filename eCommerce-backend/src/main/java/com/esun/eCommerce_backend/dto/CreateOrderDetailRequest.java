package com.esun.eCommerce_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateOrderDetailRequest {

	@NotBlank
	@Size(max = 50)
	@Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "productId contains unsupported characters")
	private String productId;

	@NotNull
	@Min(1)
	private Integer quantity;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}