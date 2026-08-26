package com.plip.topic.adapter.out.video;

import com.plip.topic.application.exception.VideoOwnershipUnavailableException;
import com.plip.topic.application.port.out.VideoOwnershipPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
public class RestVideoOwnershipAdapter implements VideoOwnershipPort {

	private static final String OWNERSHIP_PATH = "/internal/videos/{videoUuid}";

	private final RestClient videoInternalRestClient;

	public RestVideoOwnershipAdapter(@Qualifier("videoInternalRestClient") RestClient videoInternalRestClient) {
		this.videoInternalRestClient = videoInternalRestClient;
	}

	@Override
	public Optional<UUID> findOwnerUuid(UUID videoUuid) {
		if (videoUuid == null) {
			return Optional.empty();
		}
		try {
			InternalVideoOwnershipResponse body = videoInternalRestClient.get()
					.uri(OWNERSHIP_PATH, videoUuid)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
							throw new VideoNotFound();
						}
						throw new VideoOwnershipUnavailableException();
					})
					.body(InternalVideoOwnershipResponse.class);
			if (body == null || body.userUuid() == null) {
				return Optional.empty();
			}
			return Optional.of(body.userUuid());
		} catch (VideoNotFound exception) {
			return Optional.empty();
		} catch (VideoOwnershipUnavailableException exception) {
			throw exception;
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				return Optional.empty();
			}
			throw new VideoOwnershipUnavailableException(exception);
		} catch (ResourceAccessException exception) {
			throw new VideoOwnershipUnavailableException(exception);
		}
	}

	private static final class VideoNotFound extends RuntimeException {
	}
}
