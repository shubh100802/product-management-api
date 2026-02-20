package com.zest.productapi.service;

// ==========file-context==========

import com.zest.productapi.dto.ItemResponse;
import com.zest.productapi.dto.PageResponse;
import com.zest.productapi.dto.ProductCreateRequest;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.dto.ProductUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    PageResponse<ProductResponse> getProducts(String name, Pageable pageable);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);

    List<ItemResponse> getItemsByProductId(Long productId);
}

