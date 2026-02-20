package com.zest.productapi.service.impl;

// ==========file-context==========

import com.zest.productapi.dto.LoginRequest;
import com.zest.productapi.dto.RefreshTokenRequest;
import com.zest.productapi.dto.RegisterRequest;
import com.zest.productapi.entity.RefreshToken;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.RoleName;
import com.zest.productapi.entity.User;
import com.zest.productapi.exception.AuthException;
import com.zest.productapi.repository.RefreshTokenRepository;
import com.zest.productapi.repository.RoleRepository;
import com.zest.productapi.repository.UserRepository;
import com.zest.productapi.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("testassignmentsecretkeyforhs256mustbeatleast32bytes", 900000);
        authService = new AuthServiceImpl(
                userRepository,
                roleRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtUtil,
                900000,
                604800000
        );
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName(RoleName.ROLE_USER);

        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(7L);
        savedUser.setEmail("a@b.com");
        savedUser.setPassword("encoded");
        savedUser.setEnabled(true);
        savedUser.setRoles(Set.of(roleUser));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        RefreshToken persisted = new RefreshToken();
        persisted.setId(9L);
        persisted.setToken("refresh-token");
        persisted.setUser(savedUser);
        persisted.setExpiresAt(Instant.now().plusSeconds(60));
        persisted.setRevoked(false);

        when(refreshTokenRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(persisted);

        var response = authService.register(new RegisterRequest("User", "a@b.com", "password123"));

        assertNotNull(response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertTrue(response.roles().contains("ROLE_USER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("a@b.com", userCaptor.getValue().getEmail());
    }

    @Test
    void login_shouldThrowWhenUserNotFoundAfterAuth() {
        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username("missing@zest.com")
                .password("x")
                .authorities("ROLE_USER")
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("missing@zest.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class,
                () -> authService.login(new LoginRequest("missing@zest.com", "pass")));
    }

    @Test
    void refresh_shouldRejectRevokedToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("r1");
        token.setRevoked(true);
        token.setExpiresAt(Instant.now().plusSeconds(60));

        when(refreshTokenRepository.findByToken("r1")).thenReturn(Optional.of(token));

        assertThrows(AuthException.class, () -> authService.refresh(new RefreshTokenRequest("r1")));
        verify(refreshTokenRepository).save(token);
    }
}

