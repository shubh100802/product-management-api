package com.zest.productapi.dto;

// ==========file-context==========

public record ItemResponse(
        Long id,
        Long productId,
        Integer quantity
) {
}

