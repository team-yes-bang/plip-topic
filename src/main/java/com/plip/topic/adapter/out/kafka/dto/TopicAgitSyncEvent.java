package com.plip.topic.adapter.out.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopicAgitSyncEvent(
		UUID agitUuid,
		String topicId,
		Instant startedAt,
		Instant occurredAt
) {
}
