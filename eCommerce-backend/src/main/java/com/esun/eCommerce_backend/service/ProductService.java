package com.esun.eCommerce_backend.service;

import java.sql.Types;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.esun.eCommerce_backend.dto.CreateProductRequest;
import com.esun.eCommerce_backend.model.Product;

@Service
public class ProductService {

	private final JdbcTemplate jdbcTemplate;

	public ProductService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public Product createProduct(CreateProductRequest request) {
		String productId = request.getProductId().trim();
		String productName = request.getProductName().trim();

		try {
			jdbcTemplate.update(
				"CALL sp_InsertProduct(?, ?, ?, ?)",
				new SqlParameterValue(Types.VARCHAR, productId),
				new SqlParameterValue(Types.VARCHAR, productName),
				new SqlParameterValue(Types.DECIMAL, request.getPrice()),
				new SqlParameterValue(Types.INTEGER, request.getQuantity())
			);
		} catch (DuplicateKeyException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "productId already exists", ex);
		}

		return new Product(productId, productName, request.getPrice(), request.getQuantity());
	}
}