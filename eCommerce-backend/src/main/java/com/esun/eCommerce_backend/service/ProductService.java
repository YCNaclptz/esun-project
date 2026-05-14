package com.esun.eCommerce_backend.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.esun.eCommerce_backend.dto.AdjustProductQuantityRequest;
import com.esun.eCommerce_backend.dto.CreateProductRequest;
import com.esun.eCommerce_backend.dto.UpdateProductQuantityRequest;
import com.esun.eCommerce_backend.model.Product;

@Service
public class ProductService {

	private static final int MYSQL_ERR_INSUFFICIENT_STOCK = 10001;
	private static final int MYSQL_ERR_PRODUCT_NOT_FOUND = 10002;
	private static final int MYSQL_ERR_INVALID_QUANTITY = 10003;
	private static final RowMapper<Product> PRODUCT_ROW_MAPPER = ProductService::mapProduct;

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

	@Transactional
	public Product updateProductQuantity(String productId, UpdateProductQuantityRequest request) {
		String normalizedProductId = productId.trim();

		try {
			jdbcTemplate.update(
				"CALL sp_UpdateProductQuantity(?, ?)",
				new SqlParameterValue(Types.VARCHAR, normalizedProductId),
				new SqlParameterValue(Types.INTEGER, request.getQuantity())
			);
		} catch (UncategorizedSQLException ex) {
			throw mapStoredProcedureException(ex);
		}

		return loadProduct(normalizedProductId);
	}

	@Transactional
	public Product adjustProductQuantity(String productId, AdjustProductQuantityRequest request) {
		String normalizedProductId = productId.trim();

		try {
			jdbcTemplate.update(
				"CALL sp_AdjustProductQuantity(?, ?)",
				new SqlParameterValue(Types.VARCHAR, normalizedProductId),
				new SqlParameterValue(Types.INTEGER, request.getDelta())
			);
		} catch (UncategorizedSQLException ex) {
			throw mapStoredProcedureException(ex);
		}

		return loadProduct(normalizedProductId);
	}

	private Product loadProduct(String productId) {
		return jdbcTemplate.query(
			"SELECT ProductID, ProductName, Price, Quantity FROM Product WHERE ProductID = ?",
			PRODUCT_ROW_MAPPER,
			productId
		).stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
			"product not found"));
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

	private static Product mapProduct(ResultSet resultSet, int rowNum) throws SQLException {
		return new Product(
			resultSet.getString("ProductID"),
			resultSet.getString("ProductName"),
			resultSet.getBigDecimal("Price"),
			resultSet.getInt("Quantity")
		);
	}
}
