package org.example.shopapp.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens", indexes = {
		@Index(name = "idx_token_value", columnList = "token", unique = true),
		@Index(name = "idx_token_user", columnList = "user_id")
})
public class Token {

	public enum TokenType {
		REFRESH
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "token", nullable = false, length = 1024, unique = true)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private TokenType type;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked", nullable = false)
	private Boolean revoked;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}


