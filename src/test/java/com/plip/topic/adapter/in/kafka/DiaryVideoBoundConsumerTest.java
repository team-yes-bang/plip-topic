package com.plip.topic.adapter.in.kafka;

import com.plip.topic.adapter.in.kafka.dto.DiaryVideoBoundEvent;
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
class DiaryVideoBoundConsumerTest {

	@Mock
	private AttachTopicVideoUseCase attachTopicVideoUseCase;

	@InjectMocks
	private DiaryVideoBoundConsumer diaryVideoBoundConsumer;

	@Test
	void consume_attachesWhenTopicUuidPresent() {
		UUID topicUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();

		diaryVideoBoundConsumer.consume(new DiaryVideoBoundEvent(UUID.randomUUID(), topicUuid, videoUuid, userUuid));

		verify(attachTopicVideoUseCase).tryAttach(topicUuid, videoUuid, userUuid);
	}

	@Test
	void consume_skipsWhenTopicUuidMissing() {
		diaryVideoBoundConsumer.consume(new DiaryVideoBoundEvent(UUID.randomUUID(), null, UUID.randomUUID(), UUID.randomUUID()));

		verify(attachTopicVideoUseCase, never()).tryAttach(any(), any(), any());
	}

	@Test
	void consume_skipsWhenUserUuidMissing() {
		diaryVideoBoundConsumer.consume(new DiaryVideoBoundEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null));

		verify(attachTopicVideoUseCase, never()).tryAttach(any(), any(), any());
	}
}
