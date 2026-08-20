package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicCreatedEvent;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicCreatedKafkaAdapterTest {

	@Mock
	private KafkaTemplate<String, TopicCreatedEvent> topicCreatedKafkaTemplate;

	private TopicCreatedKafkaAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new TopicCreatedKafkaAdapter(topicCreatedKafkaTemplate);
		ReflectionTestUtils.setField(adapter, "topic", "topic.created");
	}

	@Test
	void publishCreated_sendsKeyTopicUuidAndTitlePayload() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		Topic topic = Topic.create(agitUuid, creatorUuid, "점심 메뉴", startAt);

		adapter.publishCreated(topic);

		ArgumentCaptor<TopicCreatedEvent> captor = ArgumentCaptor.forClass(TopicCreatedEvent.class);
		verify(topicCreatedKafkaTemplate).send(eq("topic.created"), eq(topic.getTopicUuid().toString()), captor.capture());
		TopicCreatedEvent event = captor.getValue();
		assertThat(event.topicUuid()).isEqualTo(topic.getTopicUuid());
		assertThat(event.agitUuid()).isEqualTo(agitUuid);
		assertThat(event.creatorUuid()).isEqualTo(creatorUuid);
		assertThat(event.title()).isEqualTo("점심 메뉴");
		assertThat(event.startAt()).isEqualTo(startAt);
		assertThat(event.occurredAt()).isNotNull();
	}
}
