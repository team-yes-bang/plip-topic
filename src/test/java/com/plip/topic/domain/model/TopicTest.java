package com.plip.topic.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicTest {

	@Test
	void create_assignsUuidV7AndDefaultsStartAtToToday() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();

		Topic topic = Topic.create(agitUuid, creatorUuid, "주말 모임", null);

		assertThat(topic.getTopicUuid()).isNotNull();
		assertThat(topic.getTopicUuid().version()).isEqualTo(7);
		assertThat(topic.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(topic.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(topic.getStartAt()).isEqualTo(LocalDate.now().atStartOfDay());
		assertThat(topic.getVideos()).isEmpty();
		assertThat(topic.isDeleted()).isFalse();
	}

	@Test
	void create_requiresAgitUuid() {
		assertThatThrownBy(() -> Topic.create(null, UUID.randomUUID(), "제목", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void attachVideo_addsOwnedVideo() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);

		Topic attached = topic.attachVideo(userUuid, videoUuid);

		assertThat(attached.getVideos()).hasSize(1);
		assertThat(attached.getVideos().get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(attached.getVideos().get(0).getUserUuid()).isEqualTo(userUuid);
		assertThat(attached.uploadedBy(userUuid)).isTrue();
	}

	@Test
	void attachVideo_isIdempotentForSameVideo() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(userUuid, videoUuid);

		Topic again = topic.attachVideo(userUuid, videoUuid);

		assertThat(again.getVideos()).hasSize(1);
	}

	@Test
	void attachVideo_rejectsSecondVideoFromSameUser() {
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(userUuid, UUID.randomUUID());

		assertThatThrownBy(() -> topic.attachVideo(userUuid, UUID.randomUUID()))
				.isInstanceOf(TopicVideoLimitException.class)
				.hasMessage("이미 이 토픽에 영상을 올렸습니다.");
	}

	@Test
	void detachVideo_removesOwnVideo() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(userUuid, videoUuid);

		Topic detached = topic.detachVideo(userUuid, videoUuid);

		assertThat(detached.getVideos()).isEmpty();
	}

	@Test
	void detachVideo_rejectsOtherUsersVideo() {
		UUID owner = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(owner, videoUuid);

		assertThatThrownBy(() -> topic.detachVideo(UUID.randomUUID(), videoUuid))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("본인 영상만 제거할 수 있습니다.");
	}

	@Test
	void softDelete_marksTopicDeleted() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);
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
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);

		Topic updated = topic.update("저녁 메뉴", null);

		assertThat(updated.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(updated.getStartAt()).isEqualTo(topic.getStartAt());
		assertThat(updated.getTopicUuid()).isEqualTo(topic.getTopicUuid());
		assertThat(updated.getAgitUuid()).isEqualTo(topic.getAgitUuid());
	}

	@Test
	void update_rejectsDeletedTopic() {
		Topic deleted = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.softDelete(LocalDateTime.now());

		assertThatThrownBy(() -> deleted.update("새 제목", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("삭제된 토픽은 수정할 수 없습니다.");
	}

	@Test
	void assertDeletable_allowsEmptyTopic() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);

		topic.assertDeletable();
	}

	@Test
	void assertDeletable_rejectsTopicWithVideos() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(UUID.randomUUID(), UUID.randomUUID());

		assertThatThrownBy(topic::assertDeletable)
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("영상이 있는 토픽은 삭제할 수 없습니다.");
	}
}
