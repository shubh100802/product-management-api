package com.zest.productapi.service;

// ==========file-context==========

import com.zest.productapi.dto.AuthResponse;
import com.zest.productapi.dto.LoginRequest;
import com.zest.productapi.dto.RefreshTokenRequest;
import com.zest.productapi.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}

