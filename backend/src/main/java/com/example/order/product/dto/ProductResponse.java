package com.example.order.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.order.product.domain.Product;

public record ProductResponse(
        Long id,
        String code,
        String name,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getCode(), product.getName(),
                product.getPrice(), product.getStock(), product.getCreatedAt());
    }
}
