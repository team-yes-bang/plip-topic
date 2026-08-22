package com.plip.topic.adapter.in.web;

import com.plip.topic.application.exception.UnauthenticatedActorException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

final class RequestActor {

	static final String USER_UUID_HEADER = "X-User-UUID";

	private RequestActor() {
	}

	static UUID findUserUuid(HttpServletRequest request) {
		String raw = request.getHeader(USER_UUID_HEADER);
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(raw.trim());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	static UUID requireUserUuid(HttpServletRequest request) {
		UUID userUuid = findUserUuid(request);
		if (userUuid == null) {
			throw new UnauthenticatedActorException();
		}
		return userUuid;
	}

	static String requireAuthorization(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || authorization.isBlank()) {
			throw new UnauthenticatedActorException();
		}
		return authorization;
	}
}
