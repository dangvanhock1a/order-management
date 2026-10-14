package com.example.order.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.common.BusinessException;
import com.example.order.common.NotFoundException;
import com.example.order.common.PageResponse;
import com.example.order.product.domain.Product;
import com.example.order.product.dto.ProductRequest;
import com.example.order.product.dto.ProductResponse;
import com.example.order.product.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    static final String CODE_DUPLICATED = "PRODUCT_CODE_DUPLICATED";
    static final String CODE_IMMUTABLE = "PRODUCT_CODE_IMMUTABLE";

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public PageResponse<ProductResponse> search(String keyword, Pageable pageable) {
        Page<Product> page = (keyword == null || keyword.isBlank())
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        return PageResponse.from(page.map(ProductResponse::from));
    }

    public ProductResponse get(Long id) {
        return ProductResponse.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new BusinessException(CODE_DUPLICATED, "Mã sản phẩm đã tồn tại: " + request.code());
        }
        Product saved = repository.save(
                new Product(request.code(), request.name(), request.price(), request.stock()));
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!product.getCode().equals(request.code())) {
            throw new BusinessException(CODE_IMMUTABLE, "Không được đổi mã sản phẩm");
        }
        product.update(request.name(), request.price(), request.stock());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Product find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm id=" + id));
    }
}
