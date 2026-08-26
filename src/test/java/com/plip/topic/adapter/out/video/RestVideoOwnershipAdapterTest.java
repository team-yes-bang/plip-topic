package com.plip.topic.adapter.out.video;

import com.plip.topic.application.exception.VideoOwnershipUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestVideoOwnershipAdapterTest {

	private MockRestServiceServer server;
	private RestVideoOwnershipAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://video");
		server = MockRestServiceServer.bindTo(builder).build();
		adapter = new RestVideoOwnershipAdapter(builder.build());
	}

	@Test
	void findOwnerUuid_returnsOwnerOn200() {
		UUID videoUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		server.expect(requestTo("http://video/internal/videos/" + videoUuid))
				.andRespond(withSuccess("""
						{"videoUuid":"%s","userUuid":"%s"}
						""".formatted(videoUuid, ownerUuid), MediaType.APPLICATION_JSON));

		assertThat(adapter.findOwnerUuid(videoUuid)).contains(ownerUuid);
		server.verify();
	}

	@Test
	void findOwnerUuid_returnsEmptyOn404() {
		UUID videoUuid = UUID.randomUUID();
		server.expect(requestTo("http://video/internal/videos/" + videoUuid))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThat(adapter.findOwnerUuid(videoUuid)).isEmpty();
		server.verify();
	}

	@Test
	void findOwnerUuid_throwsOn5xx() {
		UUID videoUuid = UUID.randomUUID();
		server.expect(requestTo("http://video/internal/videos/" + videoUuid))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertThatThrownBy(() -> adapter.findOwnerUuid(videoUuid))
				.isInstanceOf(VideoOwnershipUnavailableException.class);
		server.verify();
	}

	@Test
	void findOwnerUuid_returnsEmptyWhenVideoUuidNull() {
		assertThat(adapter.findOwnerUuid(null)).isEmpty();
	}
}
