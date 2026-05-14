package com.esun.eCommerce_backend.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;

import com.esun.eCommerce_backend.model.Order;

@Repository
public class OrderRepository {

	private static final RowMapper<Order> ORDER_ROW_MAPPER = OrderRepository::mapOrder;

	private final JdbcTemplate jdbcTemplate;

	public OrderRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void createOrderDetailAndReduceStock(String orderId, String memberId, String productId, Integer quantity,
			Integer payStatus) {
		jdbcTemplate.update(
			"CALL sp_CreateOrderDetailAndReduceStock(?, ?, ?, ?, ?)",
			new SqlParameterValue(Types.VARCHAR, orderId),
			new SqlParameterValue(Types.VARCHAR, memberId),
			new SqlParameterValue(Types.VARCHAR, productId),
			new SqlParameterValue(Types.INTEGER, quantity),
			new SqlParameterValue(Types.TINYINT, payStatus)
		);
	}

	public Optional<Order> findByOrderId(String orderId) {
		return jdbcTemplate.query(
			"SELECT OrderID, MemberID, Price, PayStatus FROM Order_Main WHERE OrderID = ?",
			ORDER_ROW_MAPPER,
			orderId
		).stream().findFirst();
	}

	private static Order mapOrder(ResultSet resultSet, int rowNum) throws SQLException {
		return new Order(
			resultSet.getString("OrderID"),
			resultSet.getString("MemberID"),
			resultSet.getBigDecimal("Price"),
			resultSet.getInt("PayStatus")
		);
	}
}