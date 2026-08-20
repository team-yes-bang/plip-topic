package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicVideoAttachedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicVideoAttachedKafkaAdapterTest {

	@Mock
	private KafkaTemplate<String, TopicVideoAttachedEvent> topicVideoAttachedKafkaTemplate;

	private TopicVideoAttachedKafkaAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new TopicVideoAttachedKafkaAdapter(topicVideoAttachedKafkaTemplate);
		ReflectionTestUtils.setField(adapter, "topic", "topic.video.attached");
	}

	@Test
	void publishAttached_sendsKeyVideoUuid() {
		UUID topicUuid = UUID.randomUUID();
		UUID agitUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();

		adapter.publishAttached(topicUuid, agitUuid, videoUuid, userUuid);

		ArgumentCaptor<TopicVideoAttachedEvent> captor = ArgumentCaptor.forClass(TopicVideoAttachedEvent.class);
		verify(topicVideoAttachedKafkaTemplate).send(
				eq("topic.video.attached"),
				eq(videoUuid.toString()),
				captor.capture()
		);
		TopicVideoAttachedEvent event = captor.getValue();
		assertThat(event.topicUuid()).isEqualTo(topicUuid);
		assertThat(event.agitUuid()).isEqualTo(agitUuid);
		assertThat(event.videoUuid()).isEqualTo(videoUuid);
		assertThat(event.userUuid()).isEqualTo(userUuid);
		assertThat(event.occurredAt()).isNotNull();
	}
}
