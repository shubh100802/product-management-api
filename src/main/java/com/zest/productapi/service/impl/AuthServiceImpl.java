package com.zest.productapi.service.impl;

// ==========file-context==========

import com.zest.productapi.dto.AuthResponse;
import com.zest.productapi.dto.LoginRequest;
import com.zest.productapi.dto.RefreshTokenRequest;
import com.zest.productapi.dto.RegisterRequest;
import com.zest.productapi.entity.RefreshToken;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.RoleName;
import com.zest.productapi.entity.User;
import com.zest.productapi.exception.AuthException;
import com.zest.productapi.exception.ConflictException;
import com.zest.productapi.repository.RefreshTokenRepository;
import com.zest.productapi.repository.RoleRepository;
import com.zest.productapi.repository.UserRepository;
import com.zest.productapi.security.JwtUtil;
import com.zest.productapi.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                           @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // ==========register-validation==========
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AuthException("Default role ROLE_USER is not configured"));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        return createAuthPayload(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // ==========credential-check==========
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AuthException("User not found"));

        return createAuthPayload(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        // ==========refresh-token-lookup==========
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new AuthException("Refresh token is expired or revoked");
        }

        User user = token.getUser();
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return createAuthPayload(user);
    }

    private AuthResponse createAuthPayload(User user) {
        // ==========access-token-creation==========
        String accessToken = jwtUtil.generateAccessToken(toUserDetails(user));

        RefreshToken newRefreshToken = rotateRefreshToken(user);

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                accessToken,
                newRefreshToken.getToken(),
                "Bearer",
                accessTokenExpirationMs / 1000,
                roles
        );
    }

    private RefreshToken rotateRefreshToken(User user) {
        // ==========single-refresh-token-per-user==========
        RefreshToken token = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    RefreshToken created = new RefreshToken();
                    created.setUser(user);
                    return created;
                });

        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(r -> r.getName().name()).toArray(String[]::new))
                .disabled(!user.isEnabled())
                .build();
    }
}
