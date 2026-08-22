package com.plip.topic.adapter.out.security;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;

public final class JwtSigningKeys {

	static final String FALLBACK_SECRET =
			"user-service-default-jwt-secret-key-change-me-please";

	private JwtSigningKeys() {
	}

	public static SecretKey hmacSha256(String rawSecret) {
		String raw = rawSecret;
		if (raw == null || raw.isBlank()) {
			raw = FALLBACK_SECRET;
		}
		byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			bytes = sha256(raw);
		}
		return Keys.hmacShaKeyFor(bytes);
	}

	private static byte[] sha256(String value) {
		try {
			return MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
