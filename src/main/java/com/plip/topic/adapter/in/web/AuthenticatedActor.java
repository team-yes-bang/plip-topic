package com.plip.topic.adapter.in.web;

import com.plip.topic.application.exception.UnauthenticatedActorException;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

final class AuthenticatedActor {

	private AuthenticatedActor() {
	}

	static UUID findUserUuid() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}
		try {
			return UUID.fromString(authentication.getName());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	static UUID requireUserUuid() {
		UUID userUuid = findUserUuid();
		if (userUuid == null) {
			throw new UnauthenticatedActorException();
		}
		return userUuid;
	}
}
