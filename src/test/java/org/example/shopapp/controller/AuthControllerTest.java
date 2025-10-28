package org.example.shopapp.controller;

import org.example.shopapp.dto.request.LoginRequest;
import org.example.shopapp.dto.request.RegisterRequest;
import org.example.shopapp.dto.response.AuthResponse;
import org.example.shopapp.dto.response.UserResponse;
import org.example.shopapp.entity.User;
import org.example.shopapp.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    
    @Mock
    private AuthService authService;
    
    @InjectMocks
    private AuthController authController;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    
    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .phoneNumber("+1234567890")
                .build();
        
        loginRequest = LoginRequest.builder()
                .usernameOrEmail("test@example.com")
                .password("password123")
                .build();
        
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phoneNumber("+1234567890")
                .role(User.Role.CUSTOMER)
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        authResponse = AuthResponse.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();
    }
    
    @Test
    void register_ShouldReturnCreated_WhenValidRequest() {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);
        
        // When
        ResponseEntity<?> response = authController.register(registerRequest);
        
        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void register_ShouldReturnBadRequest_WhenInvalidRequest() {
        // Given
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("") // Invalid: empty username
                .email("invalid-email") // Invalid: malformed email
                .password("123") // Invalid: too short password
                .build();
        
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Validation failed"));
        
        // When
        ResponseEntity<?> response = authController.register(invalidRequest);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void login_ShouldReturnOk_WhenValidCredentials() {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);
        
        // When
        ResponseEntity<?> response = authController.login(loginRequest);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    void login_ShouldReturnBadRequest_WhenInvalidCredentials() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));
        
        // When
        ResponseEntity<?> response = authController.login(loginRequest);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
