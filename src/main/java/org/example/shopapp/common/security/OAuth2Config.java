package org.example.shopapp.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@RequiredArgsConstructor
public class OAuth2Config {
    
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return oAuth2AuthenticationSuccessHandler;
    }
}
