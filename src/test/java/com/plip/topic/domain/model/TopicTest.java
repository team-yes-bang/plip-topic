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

		Topic topic = Topic.create(agitUuid, creatorUuid, "주말 모임", null, List.of(videoUuid));

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
		assertThatThrownBy(() -> Topic.create(null, UUID.randomUUID(), "제목", null, List.of()))
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
				List.of(videoUuid, videoUuid)
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("중복된 videoUuid는 허용되지 않습니다.");
	}

	@Test
	void softDelete_marksTopicDeleted() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of());
		LocalDateTime deletedAt = LocalDateTime.now();

		Topic deleted = topic.softDelete(deletedAt);

		assertThat(deleted.isDeleted()).isTrue();
		assertThat(deleted.getDeletedAt()).isEqualTo(deletedAt);
		assertThat(deleted.getTopicUuid()).isEqualTo(topic.getTopicUuid());
	}

	@Test
	void update_changesProvidedFieldsAndKeepsOmittedOnes() {
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0),
				List.of()
		);

		Topic updated = topic.update("저녁 메뉴", null);

		assertThat(updated.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(updated.getStartAt()).isEqualTo(topic.getStartAt());
		assertThat(updated.getTopicUuid()).isEqualTo(topic.getTopicUuid());
		assertThat(updated.getAgitUuid()).isEqualTo(topic.getAgitUuid());
	}

	@Test
	void update_rejectsDeletedTopic() {
		Topic deleted = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of())
				.softDelete(LocalDateTime.now());

		assertThatThrownBy(() -> deleted.update("새 제목", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("삭제된 토픽은 수정할 수 없습니다.");
	}

	@Test
	void assertDeletable_allowsEmptyTopic() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of());

		topic.assertDeletable();
	}

	@Test
	void assertDeletable_rejectsTopicWithVideos() {
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"제목",
				null,
				List.of(UUID.randomUUID())
		);

		assertThatThrownBy(topic::assertDeletable)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("영상이 있는 토픽은 삭제할 수 없습니다.");
	}
}
