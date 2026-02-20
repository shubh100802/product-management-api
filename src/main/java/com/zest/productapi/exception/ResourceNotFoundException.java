package com.zest.productapi.exception;

// ==========file-context==========

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

