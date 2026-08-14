package com.plip.topic.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicTest {

	@Test
	void create_assignsUuidV7AndDefaultsStartAtToToday() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();

		Topic topic = Topic.create(agitUuid, creatorUuid, "주말 모임", null, "grid", List.of(videoUuid));

		assertThat(topic.getTopicUuid()).isNotNull();
		assertThat(topic.getTopicUuid().version()).isEqualTo(7);
		assertThat(topic.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(topic.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(topic.getStartAt()).isEqualTo(LocalDate.now().atStartOfDay());
		assertThat(topic.getVideos()).hasSize(1);
		assertThat(topic.getVideos().get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(topic.isDeleted()).isFalse();
	}

	@Test
	void create_requiresAgitUuid() {
		assertThatThrownBy(() -> Topic.create(null, UUID.randomUUID(), "제목", null, null, List.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void create_rejectsDuplicateVideoUuid() {
		UUID videoUuid = UUID.randomUUID();

		assertThatThrownBy(() -> Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"제목",
				LocalDateTime.now(),
				null,
				List.of(videoUuid, videoUuid)
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("중복된 videoUuid는 허용되지 않습니다.");
	}

	@Test
	void softDelete_marksTopicDeleted() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, null, List.of());
		LocalDateTime deletedAt = LocalDateTime.now();

		Topic deleted = topic.softDelete(deletedAt);

		assertThat(deleted.isDeleted()).isTrue();
		assertThat(deleted.getDeletedAt()).isEqualTo(deletedAt);
		assertThat(deleted.getTopicUuid()).isEqualTo(topic.getTopicUuid());
	}
}
