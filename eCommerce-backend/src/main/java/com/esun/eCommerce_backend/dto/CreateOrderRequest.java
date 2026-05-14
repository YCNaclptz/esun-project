package com.esun.eCommerce_backend.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateOrderRequest {

	@NotBlank
	@Size(max = 50)
	@Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "memberId contains unsupported characters")
	private String memberId;

	@NotNull
	@Min(0)
	@Max(1)
	private Integer payStatus;

	@NotEmpty
	@Valid
	private List<CreateOrderDetailRequest> orderDetail;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}

	public List<CreateOrderDetailRequest> getOrderDetail() {
		return orderDetail;
	}

	public void setOrderDetail(List<CreateOrderDetailRequest> orderDetail) {
		this.orderDetail = orderDetail;
	}
}