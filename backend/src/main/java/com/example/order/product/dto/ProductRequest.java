package com.example.order.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu tạo/sửa sản phẩm.
 */
public record ProductRequest(
        @NotBlank(message = "Mã sản phẩm là bắt buộc")
        @Size(max = 30, message = "Mã sản phẩm tối đa 30 ký tự")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Mã sản phẩm chỉ gồm chữ in hoa, số và dấu -")
        String code,

        @NotBlank(message = "Tên sản phẩm là bắt buộc")
        @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
        String name,

        @NotNull(message = "Giá là bắt buộc")
        @DecimalMin(value = "0", message = "Giá không được âm")
        @Digits(integer = 13, fraction = 2, message = "Giá tối đa 13 chữ số phần nguyên và 2 chữ số thập phân")
        BigDecimal price,

        @NotNull(message = "Tồn kho là bắt buộc")
        @Min(value = 0, message = "Tồn kho không được âm")
        Integer stock) {
}
