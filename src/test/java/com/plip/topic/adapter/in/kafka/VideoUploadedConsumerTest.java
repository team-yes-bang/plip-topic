package com.plip.topic.adapter.in.kafka;

import com.plip.topic.adapter.in.kafka.dto.VideoUploadedEvent;
import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoUploadedConsumerTest {

	@Mock
	private AttachTopicVideoUseCase attachTopicVideoUseCase;

	@InjectMocks
	private VideoUploadedConsumer videoUploadedConsumer;

	@Test
	void consume_attachesWhenTopicUuidPresent() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();

		videoUploadedConsumer.consume(new VideoUploadedEvent(UUID.randomUUID(), topicUuid, videoUuid, UUID.randomUUID()));

		verify(attachTopicVideoUseCase).attach(topicUuid, videoUuid);
	}

	@Test
	void consume_skipsWhenTopicUuidMissing() {
		videoUploadedConsumer.consume(new VideoUploadedEvent(UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()));

		verify(attachTopicVideoUseCase, never()).attach(any(), any());
	}
}
