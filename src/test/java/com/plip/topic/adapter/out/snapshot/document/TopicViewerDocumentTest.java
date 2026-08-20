package com.plip.topic.adapter.out.snapshot.document;

import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TopicViewerDocumentTest {

	@Test
	void fromAndToDomain_roundTripsViewerJsonFields() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		).attachVideo(userUuid, videoUuid);

		TopicViewerDocument document = TopicViewerDocument.from(topic);
		Topic restored = document.toDomain();

		assertThat(document.getId()).isEqualTo(topic.getTopicUuid().toString());
		assertThat(document.getTopicUuid()).isEqualTo(topic.getTopicUuid().toString());
		assertThat(document.getAgitUuid()).isEqualTo(topic.getAgitUuid().toString());
		assertThat(document.getTitle()).isEqualTo("점심 메뉴");
		assertThat(document.getVideoCount()).isEqualTo(1);
		assertThat(document.isDeleted()).isFalse();
		assertThat(document.getVideos()).hasSize(1);
		assertThat(document.getVideos().get(0).getVideoUuid()).isEqualTo(videoUuid.toString());
		assertThat(document.getVideos().get(0).getUserUuid()).isEqualTo(userUuid.toString());
		assertThat(document.getProjectedAt()).isNotNull();

		assertThat(restored.getTopicUuid()).isEqualTo(topic.getTopicUuid());
		assertThat(restored.getAgitUuid()).isEqualTo(topic.getAgitUuid());
		assertThat(restored.getTitle()).isEqualTo("점심 메뉴");
		assertThat(restored.videoCount()).isEqualTo(1);
		assertThat(restored.getVideos().get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(restored.getVideos().get(0).getUserUuid()).isEqualTo(userUuid);
		assertThat(restored.isDeleted()).isFalse();
	}
}
