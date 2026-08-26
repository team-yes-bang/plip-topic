package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.out.agit.StubAgitMembershipAdapter;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.global.config.InternalApiKeyFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TopicInternalControllerTest {

	private static final String TEST_INTERNAL_API_KEY = "test-internal-api-key";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TopicPersistencePort topicPersistencePort;

	@Autowired
	private StubAgitMembershipAdapter stubAgitMembershipAdapter;

	@BeforeEach
	void resetMembershipStub() {
		stubAgitMembershipAdapter.reset();
	}

	@Test
	void checkAccess_returnsNoContentWhenMember() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = topicPersistencePort.save(Topic.create(
				agitUuid,
				userUuid,
				"제목",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid);

		mockMvc.perform(head("/internal/v1/videos/{videoUuid}/access/{userUuid}", videoUuid, userUuid)
						.header(InternalApiKeyFilter.HEADER_NAME, TEST_INTERNAL_API_KEY))
				.andExpect(status().isNoContent());
	}

	@Test
	void checkAccess_returnsForbiddenWhenNotMember() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID uploaderUuid = UUID.randomUUID();
		UUID viewerUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = topicPersistencePort.save(Topic.create(
				agitUuid,
				uploaderUuid,
				"제목",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, uploaderUuid);
		stubAgitMembershipAdapter.deny(viewerUuid);

		mockMvc.perform(head("/internal/v1/videos/{videoUuid}/access/{userUuid}", videoUuid, viewerUuid)
						.header(InternalApiKeyFilter.HEADER_NAME, TEST_INTERNAL_API_KEY))
				.andExpect(status().isForbidden());
	}

	@Test
	void checkAccess_returnsForbiddenWhenVideoHasNoTopicLink() throws Exception {
		mockMvc.perform(head(
						"/internal/v1/videos/{videoUuid}/access/{userUuid}",
						UUID.randomUUID(),
						UUID.randomUUID())
						.header(InternalApiKeyFilter.HEADER_NAME, TEST_INTERNAL_API_KEY))
				.andExpect(status().isForbidden());
	}

	@Test
	void checkAccess_returnsUnauthorizedWithoutApiKey() throws Exception {
		mockMvc.perform(head(
						"/internal/v1/videos/{videoUuid}/access/{userUuid}",
						UUID.randomUUID(),
						UUID.randomUUID()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void checkAccess_returnsUnauthorizedWithWrongApiKey() throws Exception {
		mockMvc.perform(head(
						"/internal/v1/videos/{videoUuid}/access/{userUuid}",
						UUID.randomUUID(),
						UUID.randomUUID())
						.header(InternalApiKeyFilter.HEADER_NAME, "wrong-key"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void checkAccess_legacyTopicVideosPathIsNotMapped() throws Exception {
		mockMvc.perform(head(
						"/internal/v1/topic-videos/{videoUuid}/access/{userUuid}",
						UUID.randomUUID(),
						UUID.randomUUID())
						.header(InternalApiKeyFilter.HEADER_NAME, TEST_INTERNAL_API_KEY))
				.andExpect(status().isNotFound());
	}
}
