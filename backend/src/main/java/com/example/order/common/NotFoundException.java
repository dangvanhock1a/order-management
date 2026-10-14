package com.example.order.common;

/**
 * Không tìm thấy dữ liệu. GlobalExceptionHandler trả về HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
