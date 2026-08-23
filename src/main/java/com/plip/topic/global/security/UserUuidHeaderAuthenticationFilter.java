package com.plip.topic.global.security;

import com.plip.topic.global.web.RequestHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UserUuidHeaderAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String userUuidHeader = request.getHeader(RequestHeaders.USER_UUID_HEADER);
		if (userUuidHeader != null && !userUuidHeader.isBlank()) {
			parseUuid(userUuidHeader.trim()).ifPresent(this::authenticate);
		}
		filterChain.doFilter(request, response);
	}

	private Optional<UUID> parseUuid(String value) {
		try {
			return Optional.of(UUID.fromString(value));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private void authenticate(UUID userUuid) {
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userUuid.toString(), null, List.of());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
