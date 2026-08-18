package com.plip.topic.application.service;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttachTopicVideoServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@Mock
	private TopicReadCachePort topicReadCachePort;

	@InjectMocks
	private AttachTopicVideoService attachTopicVideoService;

	@Test
	void attach_delegatesToPersistenceAndEvictsCache() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"제목",
				LocalDateTime.of(2026, 8, 18, 0, 0),
				List.of()
		);
		given(topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid)).willReturn(true);
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.of(topic));

		assertThat(attachTopicVideoService.attach(topicUuid, videoUuid)).isTrue();
		verify(topicPersistencePort).addVideoIfAbsent(topicUuid, videoUuid);
		verify(topicReadCachePort).evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
	}

	@Test
	void attach_skipsCacheWhenNotAttached() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		given(topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid)).willReturn(false);

		assertThat(attachTopicVideoService.attach(topicUuid, videoUuid)).isFalse();
		verify(topicReadCachePort, never()).evict(any(), any());
	}

	@Test
	void attach_skipsWhenIdsMissing() {
		assertThat(attachTopicVideoService.attach(null, UUID.randomUUID())).isFalse();
		assertThat(attachTopicVideoService.attach(UUID.randomUUID(), null)).isFalse();
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any());
	}
}
