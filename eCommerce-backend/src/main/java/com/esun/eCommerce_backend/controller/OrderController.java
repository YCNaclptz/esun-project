package com.esun.eCommerce_backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esun.eCommerce_backend.dto.CreateOrderRequest;
import com.esun.eCommerce_backend.model.Order;
import com.esun.eCommerce_backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		Order savedOrder = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
	}
}