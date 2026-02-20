package com.zest.productapi.service;

// ==========file-context==========

public interface AuditLogService {
    void logProductEvent(String action, Long productId, String actor);
}

