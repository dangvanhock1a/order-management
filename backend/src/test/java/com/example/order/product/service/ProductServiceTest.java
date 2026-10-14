package com.example.order.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.order.common.BusinessException;
import com.example.order.common.NotFoundException;
import com.example.order.product.domain.Product;
import com.example.order.product.dto.ProductRequest;
import com.example.order.product.dto.ProductResponse;
import com.example.order.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private final ProductRequest request =
            new ProductRequest("SP-100", "Bút chì", new BigDecimal("3000"), 10);

    @Test
    void should_createProduct_when_codeIsNew() {
        when(repository.existsByCode("SP-100")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = service.create(request);

        assertThat(result.code()).isEqualTo("SP-100");
        assertThat(result.stock()).isEqualTo(10);
    }

    @Test
    void should_rejectCreate_when_codeAlreadyExists() {
        when(repository.existsByCode("SP-100")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ProductService.CODE_DUPLICATED);
        verify(repository, never()).save(any());
    }

    @Test
    void should_rejectUpdate_when_codeIsChanged() {
        Product existing = new Product("SP-001", "Bút bi xanh", new BigDecimal("5000"), 500);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ProductService.CODE_IMMUTABLE);
    }

    @Test
    void should_throwNotFound_when_productDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }
}
