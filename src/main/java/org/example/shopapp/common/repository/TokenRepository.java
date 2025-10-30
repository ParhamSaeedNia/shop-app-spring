package org.example.shopapp.common.repository;

import org.example.shopapp.common.entity.Token;
import org.example.shopapp.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Optional<Token> findByTokenAndTypeAndRevokedFalse(String token, Token.TokenType type);
	long deleteByUserAndType(User user, Token.TokenType type);
	long deleteByExpiresAtBefore(LocalDateTime cutoff);
}


