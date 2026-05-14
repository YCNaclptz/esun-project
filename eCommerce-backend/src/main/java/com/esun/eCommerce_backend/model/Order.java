package com.esun.eCommerce_backend.model;

import java.math.BigDecimal;

public class Order {

	private String orderId;
	private String memberId;
	private BigDecimal price;
	private Integer payStatus;

	public Order() {
	}

	public Order(String orderId, String memberId, BigDecimal price, Integer payStatus) {
		this.orderId = orderId;
		this.memberId = memberId;
		this.price = price;
		this.payStatus = payStatus;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}
}