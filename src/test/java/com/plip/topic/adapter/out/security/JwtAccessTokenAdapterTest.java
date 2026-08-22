package com.plip.topic.adapter.out.security;

import com.plip.topic.global.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAccessTokenAdapterTest {

	private static final String SECRET = "test-jwt-secret-key-for-unit-tests-only";

	private JwtAccessTokenAdapter adapter;

	@BeforeEach
	void setUp() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret(SECRET);
		adapter = new JwtAccessTokenAdapter(properties);
	}

	@Test
	void parseAccessToken_returnsUserUuid() {
		UUID userUuid = UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e");
		String token = accessToken(userUuid);

		assertEquals(userUuid, adapter.parseAccessToken(token).orElseThrow());
	}

	@Test
	void parseAccessToken_rejectsRefreshToken() {
		UUID userUuid = UUID.randomUUID();
		String token = signedToken(userUuid, "refresh");

		assertTrue(adapter.parseAccessToken(token).isEmpty());
	}

	@Test
	void parseAccessToken_rejectsGarbage() {
		assertTrue(adapter.parseAccessToken("not-a-jwt").isEmpty());
	}

	private static String accessToken(UUID userUuid) {
		return signedToken(userUuid, "access");
	}

	private static String signedToken(UUID userUuid, String tokenType) {
		Date now = new Date();
		return Jwts.builder()
				.subject(userUuid.toString())
				.claim("user_uuid", userUuid.toString())
				.claim("tokenType", tokenType)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 3_600_000L))
				.signWith(JwtSigningKeys.hmacSha256(SECRET))
				.compact();
	}
}
