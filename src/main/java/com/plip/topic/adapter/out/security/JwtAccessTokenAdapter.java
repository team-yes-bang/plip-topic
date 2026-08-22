package com.plip.topic.adapter.out.security;

import com.plip.topic.application.port.out.AccessTokenPort;
import com.plip.topic.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenAdapter implements AccessTokenPort {

	private static final String CLAIM_USER_UUID = "user_uuid";
	private static final String CLAIM_TOKEN_TYPE = "tokenType";
	private static final String TOKEN_TYPE_ACCESS = "access";

	private final JwtProperties properties;

	@Override
	public Optional<UUID> parseAccessToken(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			Claims claims = Jwts.parser()
					.verifyWith(JwtSigningKeys.hmacSha256(properties.getSecret()))
					.build()
					.parseSignedClaims(token)
					.getPayload();

			if (!TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))) {
				return Optional.empty();
			}

			String userUuid = claims.get(CLAIM_USER_UUID, String.class);
			if (userUuid == null || userUuid.isBlank()) {
				return Optional.empty();
			}
			return Optional.of(UUID.fromString(userUuid));
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
