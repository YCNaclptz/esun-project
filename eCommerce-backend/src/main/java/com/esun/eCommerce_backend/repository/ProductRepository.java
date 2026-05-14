package com.esun.eCommerce_backend.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.esun.eCommerce_backend.model.Product;

public interface ProductRepository extends ListCrudRepository<Product, String> {
}