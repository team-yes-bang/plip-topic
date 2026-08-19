package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicAgitSyncEvent;
import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class TopicAgitSyncKafkaAdapter implements TopicAgitSyncEventPort {

	private final KafkaTemplate<String, TopicAgitSyncEvent> topicAgitSyncKafkaTemplate;

	@Value("${app.kafka.topics.topic-bound:topic.bound}")
	private String boundTopic;

	@Value("${app.kafka.topics.topic-started:topic.started}")
	private String startedTopic;

	@Value("${app.kafka.topics.topic-unbound:topic.unbound}")
	private String unboundTopic;

	@Override
	public void publishBoundAndStarted(Topic saved) {
		Instant startedAt = saved.getStartAt() == null
				? null
				: saved.getStartAt().toInstant(ZoneOffset.UTC);
		TopicAgitSyncEvent event = agitSyncEvent(saved, startedAt);
		String key = saved.getAgitUuid().toString();
		topicAgitSyncKafkaTemplate.send(boundTopic, key, event);
		topicAgitSyncKafkaTemplate.send(startedTopic, key, event);
	}

	@Override
	public void publishUnbound(Topic topic) {
		topicAgitSyncKafkaTemplate.send(
				unboundTopic,
				topic.getAgitUuid().toString(),
				agitSyncEvent(topic, null)
		);
	}

	private static TopicAgitSyncEvent agitSyncEvent(Topic topic, Instant startedAt) {
		return new TopicAgitSyncEvent(
				topic.getAgitUuid(),
				topic.getTopicUuid().toString(),
				startedAt,
				Instant.now()
		);
	}
}
