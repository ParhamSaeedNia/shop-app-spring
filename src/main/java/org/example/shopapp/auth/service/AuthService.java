package org.example.shopapp.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.auth.dto.request.LoginRequest;
import org.example.shopapp.auth.dto.request.RegisterRequest;
import org.example.shopapp.auth.dto.response.AuthResponse;
import org.example.shopapp.auth.dto.response.UserResponse;
import org.example.shopapp.common.entity.User;
import org.example.shopapp.common.exception.UserNotFoundException;
import org.example.shopapp.auth.repository.UserRepository;
import org.example.shopapp.common.entity.Token;
import org.example.shopapp.common.repository.TokenRepository;
import org.example.shopapp.common.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.CUSTOMER)
                .isEnabled(true)
                .isAccountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        user = userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        persistRefreshToken(user, refreshToken);
        
        UserResponse userResponse = mapToUserResponse(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = (User) authentication.getPrincipal();
        
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // revoke old refresh tokens for the user
        tokenRepository.deleteByUserAndType(user, Token.TokenType.REFRESH);
        persistRefreshToken(user, refreshToken);
        
        UserResponse userResponse = mapToUserResponse(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();
    }

    private void persistRefreshToken(User user, String refreshToken) {
        Token token = Token.builder()
                .user(user)
                .token(refreshToken)
                .type(Token.TokenType.REFRESH)
                .expiresAt(java.time.LocalDateTime.now().plusDays(1))
                .revoked(false)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        tokenRepository.save(token);
    }
    
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UserNotFoundException("Authentication required. Please provide a valid JWT token.");
        }
        
        try {
            // If principal is a User object (from JWT authentication)
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return mapToUserResponse(user);
            }
            
            // If principal is a string (username), load user from database
            if (authentication.getPrincipal() instanceof String) {
                String username = (String) authentication.getPrincipal();
                User user = userRepository.findByEmail(username)
                        .orElseGet(() -> userRepository.findByUsername(username)
                                .orElseThrow(() -> new UserNotFoundException("User not found")));
                return mapToUserResponse(user);
            }
            
            throw new UserNotFoundException("Invalid authentication context. Please log in again.");
            
        } catch (ClassCastException e) {
            log.error("Authentication principal type error: {}", e.getMessage());
            throw new UserNotFoundException("Authentication error. Please log in again.");
        }
    }

    @Transactional
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            tokenRepository.deleteByUserAndType(user, Token.TokenType.REFRESH);
        }
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isEnabled(user.getIsEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
