package com.plip.topic.application.service;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@InjectMocks
	private TopicService topicService;

	@Test
	void listByAgitUuid_mapsPersistedTopics() {
		UUID agitUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"주말 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				"grid",
				List.of(videoUuid)
		);
		given(topicPersistencePort.findAllByAgitUuid(agitUuid)).willReturn(List.of(topic));

		var results = topicService.listByAgitUuid(agitUuid);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTitle()).isEqualTo("주말 모임");
		assertThat(results.get(0).getAgitUuid()).isEqualTo(agitUuid);
		assertThat(results.get(0).getVideoUuids()).containsExactly(videoUuid);
	}

	@Test
	void listByAgitUuid_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.listByAgitUuid(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}
}
