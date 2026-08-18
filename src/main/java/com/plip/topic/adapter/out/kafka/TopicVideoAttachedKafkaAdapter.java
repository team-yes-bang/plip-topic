package com.plip.topic.adapter.out.kafka;

import com.plip.topic.adapter.out.kafka.dto.TopicVideoAttachedEvent;
import com.plip.topic.application.port.out.TopicVideoEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class TopicVideoAttachedKafkaAdapter implements TopicVideoEventPort {

	private final KafkaTemplate<String, TopicVideoAttachedEvent> topicVideoAttachedKafkaTemplate;

	@Value("${app.kafka.topics.topic-video-attached:topic.video.attached}")
	private String topic;

	@Override
	public void publishAttached(UUID topicUuid, UUID agitUuid, UUID videoUuid, UUID userUuid) {
		TopicVideoAttachedEvent event = new TopicVideoAttachedEvent(
				topicUuid,
				agitUuid,
				videoUuid,
				userUuid,
				LocalDateTime.now()
		);
		topicVideoAttachedKafkaTemplate.send(topic, videoUuid.toString(), event);
	}
}
