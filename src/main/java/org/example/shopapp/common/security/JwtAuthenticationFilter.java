package org.example.shopapp.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.common.entity.Token;
import org.example.shopapp.common.entity.User;
import org.example.shopapp.common.repository.TokenRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String accessToken = CookieUtil.getCookie(request, CookieUtil.ACCESS_TOKEN_COOKIE).orElse(null);
            if (StringUtils.hasText(accessToken) && jwtUtil.validateToken(accessToken)) {
                authenticateWithToken(accessToken, request);
            } else {
                // attempt refresh on expired or missing access token
                String refreshToken = CookieUtil.getCookie(request, CookieUtil.REFRESH_TOKEN_COOKIE).orElse(null);
                if (StringUtils.hasText(refreshToken) && jwtUtil.validateToken(refreshToken) && jwtUtil.isRefreshToken(refreshToken)) {
                    tokenRepository.findByTokenAndTypeAndRevokedFalse(refreshToken, Token.TokenType.REFRESH).ifPresent(stored -> {
                        String username = jwtUtil.extractUsername(refreshToken);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        // rotate tokens
                        String newAccess = jwtUtil.generateToken(userDetails);
                        String newRefresh = jwtUtil.generateRefreshToken(userDetails);
                        // revoke all old refresh tokens for this user
                        if (userDetails instanceof User) {
                            tokenRepository.deleteByUserAndType((User) userDetails, Token.TokenType.REFRESH);
                            persistToken((User) userDetails, newRefresh);
                        }
                        // set cookies
                        boolean secure = true;
                        String sameSite = "Lax";
                        CookieUtil.addHttpOnlyCookie(response, CookieUtil.ACCESS_TOKEN_COOKIE, newAccess, 60 * 60, secure, sameSite, "/", null);
                        CookieUtil.addHttpOnlyCookie(response, CookieUtil.REFRESH_TOKEN_COOKIE, newRefresh, 24 * 60 * 60, secure, sameSite, "/", null);
                        // authenticate with new access token
                        authenticateWithToken(newAccess, request);
                    });
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }
    
    private void authenticateWithToken(String jwt, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void persistToken(User user, String refreshToken) {
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
}
