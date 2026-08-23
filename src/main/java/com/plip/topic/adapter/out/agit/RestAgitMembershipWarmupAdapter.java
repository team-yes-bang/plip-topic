package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.exception.AgitMembershipUnavailableException;
import com.plip.topic.application.port.out.AgitMembershipWarmupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RestAgitMembershipWarmupAdapter implements AgitMembershipWarmupPort {

	private static final String MEMBERS_PATH = "/internal/v1/agits/{agitUuid}/members";

	private final RestClient agitInternalRestClient;

	@Override
	public void warmup(UUID agitUuid) {
		if (agitUuid == null) {
			throw new AgitMembershipUnavailableException();
		}
		try {
			agitInternalRestClient.get()
					.uri(MEMBERS_PATH, agitUuid)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						throw new AgitMembershipUnavailableException();
					})
					.toBodilessEntity();
		} catch (AgitMembershipUnavailableException exception) {
			throw exception;
		} catch (ResourceAccessException | RestClientResponseException exception) {
			throw new AgitMembershipUnavailableException(exception);
		}
	}
}
