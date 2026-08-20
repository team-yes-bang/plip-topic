package com.plip.topic.application.service;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicVideoEventPort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideoLimitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicVideoServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@Mock
	private TopicReadCachePort topicReadCachePort;

	@Mock
	private TopicViewerSnapshotPort topicViewerSnapshotPort;

	@Mock
	private TopicVideoEventPort topicVideoEventPort;

	@InjectMocks
	private TopicVideoService topicVideoService;

	@Test
	void tryAttach_delegatesToPersistenceAndEvictsCache() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"제목",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);
		given(topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid, userUuid)).willReturn(true);
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.of(topic));

		assertThat(topicVideoService.tryAttach(topicUuid, videoUuid, userUuid)).isTrue();
		verify(topicPersistencePort).addVideoIfAbsent(topicUuid, videoUuid, userUuid);
		verify(topicReadCachePort).evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		verify(topicViewerSnapshotPort).delete(topicUuid);
		verify(topicVideoEventPort, never()).publishAttached(any(), any(), any(), any());
	}

	@Test
	void tryAttach_swallowsUserLimit() {
		given(topicPersistencePort.addVideoIfAbsent(any(), any(), any())).willThrow(new TopicVideoLimitException());

		assertThat(topicVideoService.tryAttach(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())).isFalse();
		verify(topicReadCachePort, never()).evict(any(), any());
	}

	@Test
	void attachOrThrow_requiresExistingTopic() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicVideoService.attachOrThrow(topicUuid, UUID.randomUUID(), UUID.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void detach_removesVideoAndEvicts() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		topicVideoService.detach(topic.getTopicUuid(), videoUuid, userUuid);

		verify(topicPersistencePort).removeVideo(topic.getTopicUuid(), videoUuid, userUuid);
		verify(topicReadCachePort).evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		verify(topicViewerSnapshotPort).delete(topic.getTopicUuid());
	}

	@Test
	void list_usesSnapshotWithoutPersistence() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0))
				.attachVideo(userUuid, videoUuid);
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		var results = topicVideoService.list(topic.getTopicUuid());

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(results.get(0).getUserUuid()).isEqualTo(userUuid);
		verify(topicPersistencePort, never()).findByTopicUuid(any());
	}

	@Test
	void list_loadsPersistenceOnSnapshotMiss() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.empty());
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		var results = topicVideoService.list(topic.getTopicUuid());

		assertThat(results).isEmpty();
		verify(topicViewerSnapshotPort).save(topic);
	}
}
