package com.example.order.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.order.common.BusinessException;
import com.example.order.product.dto.ProductResponse;
import com.example.order.product.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String VALID_BODY = """
            {"code":"SP-100","name":"Bút chì","price":3000,"stock":10}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void should_return201_when_requestIsValid() throws Exception {
        when(service.create(any())).thenReturn(new ProductResponse(1L, "SP-100", "Bút chì",
                new BigDecimal("3000.00"), 10, LocalDateTime.of(2026, 10, 13, 9, 0)));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SP-100"));
    }

    @Test
    void should_return400_when_requestIsInvalid() throws Exception {
        String invalid = """
                {"code":"sp 100","name":"","price":-1,"stock":-5}
                """;

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.code").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stock").exists());
    }

    @Test
    void should_return409_when_codeIsDuplicated() throws Exception {
        when(service.create(any())).thenThrow(
                new BusinessException("PRODUCT_CODE_DUPLICATED", "Mã sản phẩm đã tồn tại: SP-100"));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_CODE_DUPLICATED"));
    }
}
