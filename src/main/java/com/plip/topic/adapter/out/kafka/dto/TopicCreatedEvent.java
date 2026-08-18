package com.plip.topic.adapter.out.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TopicCreatedEvent(
		UUID topicUuid,
		UUID agitUuid,
		UUID creatorUuid,
		String title,
		LocalDateTime startAt,
		LocalDateTime occurredAt
) {
}
