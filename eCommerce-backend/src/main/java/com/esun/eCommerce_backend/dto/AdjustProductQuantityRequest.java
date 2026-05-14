package com.esun.eCommerce_backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class AdjustProductQuantityRequest {

	@NotNull
	private Integer delta;

	public Integer getDelta() {
		return delta;
	}

	public void setDelta(Integer delta) {
		this.delta = delta;
	}

	@AssertTrue(message = "delta must not be 0")
	public boolean isDeltaNotZero() {
		return delta != null && delta != 0;
	}
}
