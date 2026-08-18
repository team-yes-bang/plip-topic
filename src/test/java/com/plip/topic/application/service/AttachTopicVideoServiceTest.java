package com.plip.topic.application.service;

import com.plip.topic.application.port.out.TopicPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

	@InjectMocks
	private AttachTopicVideoService attachTopicVideoService;

	@Test
	void attach_delegatesToPersistence() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		given(topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid)).willReturn(true);

		assertThat(attachTopicVideoService.attach(topicUuid, videoUuid)).isTrue();
		verify(topicPersistencePort).addVideoIfAbsent(topicUuid, videoUuid);
	}

	@Test
	void attach_skipsWhenIdsMissing() {
		assertThat(attachTopicVideoService.attach(null, UUID.randomUUID())).isFalse();
		assertThat(attachTopicVideoService.attach(UUID.randomUUID(), null)).isFalse();
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any());
	}
}
