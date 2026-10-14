package com.example.order.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.order.product.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCode(String code);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
