package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.exception.AgitMembershipUnavailableException;
import com.plip.topic.application.exception.UnauthenticatedActorException;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class GatewayAgitMembershipAdapter implements AgitMembershipPort {

	private static final String AGIT_DETAIL_PATH = "/api/agit/api/v1/agits/{agitUuid}";

	private final RestClient agitGatewayRestClient;

	@Override
	public Optional<AgitMembership> findActiveMember(UUID agitUuid, String authorization) {
		if (agitUuid == null) {
			return Optional.empty();
		}
		try {
			AgitDetailClientResponse body = agitGatewayRestClient.get()
					.uri(AGIT_DETAIL_PATH, agitUuid)
					.header(HttpHeaders.AUTHORIZATION, authorization)
					.retrieve()
					.onStatus(status -> status.value() == 401, (request, response) -> {
						throw new UnauthenticatedActorException();
					})
					.onStatus(status -> status.is4xxClientError(), (request, response) -> {
						throw new MembershipRejectedException();
					})
					.onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
						throw new AgitMembershipUnavailableException();
					})
					.body(AgitDetailClientResponse.class);
			return toMembership(body);
		} catch (UnauthenticatedActorException | AgitMembershipUnavailableException exception) {
			throw exception;
		} catch (MembershipRejectedException exception) {
			return Optional.empty();
		} catch (ResourceAccessException | RestClientResponseException exception) {
			throw new AgitMembershipUnavailableException(exception);
		}
	}

	private Optional<AgitMembership> toMembership(AgitDetailClientResponse body) {
		if (body == null || body.getMyRole() == null || body.getMyRole().isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(new AgitMembership(AgitMemberRole.valueOf(body.getMyRole().trim())));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	private static final class MembershipRejectedException extends RuntimeException {
	}
}
