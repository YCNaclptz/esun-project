package com.esun.eCommerce_backend.service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esun.eCommerce_backend.dto.CreateOrderDetailRequest;
import com.esun.eCommerce_backend.dto.CreateOrderRequest;
import com.esun.eCommerce_backend.model.Order;
import com.esun.eCommerce_backend.repository.OrderRepository;

@Service
public class OrderService {
	private static final int MYSQL_ERR_INSUFFICIENT_STOCK = 10001;
	private static final int MYSQL_ERR_PRODUCT_NOT_FOUND = 10002;
	private static final int MYSQL_ERR_INVALID_QUANTITY = 10003;

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	public Order createOrder(CreateOrderRequest request) {
		String orderId = generateOrderId();
		String memberId = request.getMemberId().trim();
		Integer payStatus = request.getPayStatus();

		try {
			for (CreateOrderDetailRequest orderDetail : request.getOrderDetail()) {
				orderRepository.createOrderDetailAndReduceStock(
					orderId,
					memberId,
					orderDetail.getProductId().trim(),
					orderDetail.getQuantity(),
					payStatus
				);
			}
		} catch (UncategorizedSQLException ex) {
			throw mapStoredProcedureException(ex);
		}

		Optional<Order> savedOrder = orderRepository.findByOrderId(orderId);
		return savedOrder.orElseThrow(() -> new ResponseStatusException(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"failed to load created order"
		));
	}

	private String generateOrderId() {
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		return "ORD" + suffix;
	}

	private RuntimeException mapStoredProcedureException(UncategorizedSQLException ex) {
		SQLException sqlException = ex.getSQLException();
		if (sqlException == null) {
			return ex;
		}

		return switch (sqlException.getErrorCode()) {
			case MYSQL_ERR_INSUFFICIENT_STOCK -> new ResponseStatusException(HttpStatus.CONFLICT,
				"insufficient stock", ex);
			case MYSQL_ERR_PRODUCT_NOT_FOUND -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				"product not found", ex);
			case MYSQL_ERR_INVALID_QUANTITY -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"invalid quantity", ex);
			default -> ex;
		};
	}
}