package com.zest.productapi.dto;

// ==========file-context==========

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must be at most 255 characters")
        String productName,

        @NotBlank(message = "modifiedBy is required")
        @Size(max = 100, message = "modifiedBy must be at most 100 characters")
        String modifiedBy
) {
}

