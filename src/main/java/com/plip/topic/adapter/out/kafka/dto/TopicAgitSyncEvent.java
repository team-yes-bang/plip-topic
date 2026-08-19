package com.plip.topic.adapter.out.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record TopicAgitSyncEvent(
		UUID agitUuid,
		String topicId,
		Instant startedAt,
		Instant occurredAt
) {
}
