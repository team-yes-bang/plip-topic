package com.plip.topic.adapter.out.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TopicVideoAttachedEvent(
		UUID topicUuid,
		UUID agitUuid,
		UUID videoUuid,
		UUID userUuid,
		LocalDateTime occurredAt
) {
}
