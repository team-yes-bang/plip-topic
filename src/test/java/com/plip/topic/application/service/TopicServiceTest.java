package com.plip.topic.application.service;

import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

	@Test
	void create_persistsTopicAndReturnsResult() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		given(topicPersistencePort.save(any(Topic.class))).willAnswer(invocation -> invocation.getArgument(0));

		var result = topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(agitUuid)
				.creatorUuid(creatorUuid)
				.title("점심 메뉴")
				.startAt(startAt)
				.layout("grid")
				.videoUuids(List.of(videoUuid))
				.build());

		assertThat(result.getTopicUuid()).isNotNull();
		assertThat(result.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(result.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(result.getTitle()).isEqualTo("점심 메뉴");
		assertThat(result.getStartAt()).isEqualTo(startAt);
		assertThat(result.getLayout()).isEqualTo("grid");
		assertThat(result.getVideoUuids()).containsExactly(videoUuid);
		verify(topicPersistencePort).save(any(Topic.class));
	}

	@Test
	void create_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.creatorUuid(UUID.randomUUID())
				.title("제목")
				.build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void create_requiresCreatorUuid() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(UUID.randomUUID())
				.title("제목")
				.build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("creatorUuid는 필수입니다.");
	}
}
