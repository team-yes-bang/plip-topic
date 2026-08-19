package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicAgitSyncEvent;
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
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicAgitSyncKafkaAdapterTest {

	@Mock
	private KafkaTemplate<String, TopicAgitSyncEvent> topicAgitSyncKafkaTemplate;

	private TopicAgitSyncKafkaAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new TopicAgitSyncKafkaAdapter(topicAgitSyncKafkaTemplate);
		ReflectionTestUtils.setField(adapter, "boundTopic", "topic.bound");
		ReflectionTestUtils.setField(adapter, "startedTopic", "topic.started");
		ReflectionTestUtils.setField(adapter, "unboundTopic", "topic.unbound");
	}

	@Test
	void publishBoundAndStarted_sendsAgitContractPayloadToBothTopics() {
		UUID agitUuid = UUID.randomUUID();
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		Topic topic = Topic.create(agitUuid, UUID.randomUUID(), "점심 메뉴", startAt);

		adapter.publishBoundAndStarted(topic);

		ArgumentCaptor<TopicAgitSyncEvent> eventCaptor = ArgumentCaptor.forClass(TopicAgitSyncEvent.class);
		verify(topicAgitSyncKafkaTemplate).send(eq("topic.bound"), eq(agitUuid.toString()), eventCaptor.capture());
		verify(topicAgitSyncKafkaTemplate).send(eq("topic.started"), eq(agitUuid.toString()), eventCaptor.capture());

		assertThat(eventCaptor.getAllValues()).hasSize(2);
		eventCaptor.getAllValues().forEach(event -> {
			assertThat(event.agitUuid()).isEqualTo(agitUuid);
			assertThat(event.topicId()).isEqualTo(topic.getTopicUuid().toString());
			assertThat(event.startedAt()).isEqualTo(startAt.toInstant(ZoneOffset.UTC));
			assertThat(event.occurredAt()).isNotNull();
		});
	}

	@Test
	void publishUnbound_sendsAgitContractPayloadWithoutStartedAt() {
		UUID agitUuid = UUID.randomUUID();
		Topic topic = Topic.create(agitUuid, UUID.randomUUID(), "점심 메뉴", LocalDateTime.of(2026, 8, 18, 0, 0));

		adapter.publishUnbound(topic);

		ArgumentCaptor<TopicAgitSyncEvent> eventCaptor = ArgumentCaptor.forClass(TopicAgitSyncEvent.class);
		verify(topicAgitSyncKafkaTemplate).send(eq("topic.unbound"), eq(agitUuid.toString()), eventCaptor.capture());
		TopicAgitSyncEvent event = eventCaptor.getValue();
		assertThat(event.agitUuid()).isEqualTo(agitUuid);
		assertThat(event.topicId()).isEqualTo(topic.getTopicUuid().toString());
		assertThat(event.startedAt()).isNull();
		assertThat(event.occurredAt()).isNotNull();
	}
}
