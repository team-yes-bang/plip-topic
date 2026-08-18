package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicCreatedEvent;
import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class TopicCreatedKafkaAdapter implements TopicCreatedEventPort {

	private final KafkaTemplate<String, TopicCreatedEvent> topicCreatedKafkaTemplate;

	@Value("${app.kafka.topics.topic-created:topic.created}")
	private String topic;

	@Override
	public void publishCreated(Topic saved) {
		TopicCreatedEvent event = new TopicCreatedEvent(
				saved.getTopicUuid(),
				saved.getAgitUuid(),
				saved.getCreatorUuid(),
				saved.getTitle(),
				saved.getStartAt(),
				LocalDateTime.now()
		);
		topicCreatedKafkaTemplate.send(topic, saved.getTopicUuid().toString(), event);
	}
}
