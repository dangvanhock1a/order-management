package com.example.order.common;

import java.util.Map;

/**
 * Định dạng lỗi thống nhất trả về cho client.
 *
 * @param code        mã lỗi (VALIDATION_ERROR, NOT_FOUND hoặc mã nghiệp vụ như PRODUCT_CODE_DUPLICATED)
 * @param message     thông báo dễ hiểu
 * @param fieldErrors lỗi theo từng trường (chỉ có khi validate thất bại)
 */
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, Map.of());
    }
}
