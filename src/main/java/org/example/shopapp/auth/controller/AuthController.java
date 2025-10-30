package org.example.shopapp.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.auth.dto.request.LoginRequest;
import org.example.shopapp.auth.dto.request.RegisterRequest;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.auth.dto.response.AuthResponse;
import org.example.shopapp.auth.dto.response.UserResponse;
import org.example.shopapp.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.example.shopapp.common.security.CookieUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided information")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse auth = authService.register(request);
            org.springframework.http.HttpHeaders headers = buildCookieHeaders(auth);
            // Do not return tokens to client
            auth.setAccessToken(null);
            auth.setRefreshToken(null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(ApiResponse.success("User registered successfully", auth));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse auth = authService.login(request);
            org.springframework.http.HttpHeaders headers = buildCookieHeaders(auth);
            auth.setAccessToken(null);
            auth.setRefreshToken(null);
            return ResponseEntity.ok().headers(headers).body(ApiResponse.success("Login successful", auth));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private org.springframework.http.HttpHeaders buildCookieHeaders(AuthResponse auth) {
        boolean secure = true; // set to true in production; for local HTTP set false
        String sameSite = "Lax";
        int accessMaxAge = 60 * 60; // 1 hour (matches jwt.expiration if configured accordingly)
        int refreshMaxAge = 24 * 60 * 60; // 1 day
        String accessHeader = CookieUtil.buildSetCookieHeader(CookieUtil.ACCESS_TOKEN_COOKIE, auth.getAccessToken(), accessMaxAge, secure, sameSite, "/", null);
        String refreshHeader = CookieUtil.buildSetCookieHeader(CookieUtil.REFRESH_TOKEN_COOKIE, auth.getRefreshToken(), refreshMaxAge, secure, sameSite, "/", null);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Set-Cookie", accessHeader);
        headers.add("Set-Cookie", refreshHeader);
        return headers;
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> logout() {
        try {
            authService.logout();
            boolean secure = true;
            String sameSite = "Lax";
            String clearAccess = CookieUtil.buildSetCookieHeader(CookieUtil.ACCESS_TOKEN_COOKIE, "", 0, secure, sameSite, "/", null);
            String clearRefresh = CookieUtil.buildSetCookieHeader(CookieUtil.REFRESH_TOKEN_COOKIE, "", 0, secure, sameSite, "/", null);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add("Set-Cookie", clearAccess);
            headers.add("Set-Cookie", clearRefresh);
            return ResponseEntity.ok().headers(headers).body(ApiResponse.success("Logged out", "OK"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's information")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        try {
            UserResponse user = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
