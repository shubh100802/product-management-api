package com.zest.productapi.service.impl;

// ==========file-context==========

import com.zest.productapi.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Override
    @Async("auditTaskExecutor")
    public void logProductEvent(String action, Long productId, String actor) {
        log.info("audit action={} productId={} actor={}", action, productId, actor == null ? "system" : actor);
    }
}

