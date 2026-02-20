package com.zest.productapi.mapper;

// ==========file-context==========

import com.zest.productapi.dto.ItemResponse;
import com.zest.productapi.dto.ProductResponse;
import com.zest.productapi.dto.ProductUpdateRequest;
import com.zest.productapi.entity.Item;
import com.zest.productapi.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public void applyUpdates(Product product, ProductUpdateRequest request) {
        product.setProductName(request.productName());
        product.setModifiedBy(request.modifiedBy());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn()
        );
    }

    public ItemResponse toResponse(Item item) {
        return new ItemResponse(item.getId(), item.getProduct().getId(), item.getQuantity());
    }
}

