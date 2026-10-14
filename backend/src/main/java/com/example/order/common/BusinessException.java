package com.example.order.common;

/**
 * Lỗi nghiệp vụ (vi phạm business rule). GlobalExceptionHandler trả về HTTP 409.
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
