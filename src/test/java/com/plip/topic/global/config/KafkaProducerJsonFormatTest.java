package com.plip.topic.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.topic.adapter.out.kafka.dto.TopicAgitSyncEvent;
import com.plip.topic.adapter.out.kafka.dto.TopicCreatedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaProducerJsonFormatTest {

	private final ObjectMapper objectMapper = KafkaProducerConfig.isoObjectMapper();

	@Test
	void createdEvent_serializesDateTimesAsIso8601Strings() throws Exception {
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		TopicCreatedEvent event = new TopicCreatedEvent(
				UUID.fromString("0190abcd-1111-7abc-def0-123456789abc"),
				UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e"),
				UUID.fromString("01912345-6789-7abc-def0-123456789abe"),
				"점심 메뉴",
				startAt,
				LocalDateTime.of(2026, 8, 18, 17, 0)
		);

		String json = objectMapper.writeValueAsString(event);

		assertThat(json).contains("\"startAt\":\"2026-08-18T00:00:00\"");
		assertThat(json).contains("\"occurredAt\":\"2026-08-18T17:00:00\"");
		assertThat(json).doesNotContain("[2026,8,18");
	}

	@Test
	void agitSyncEvent_serializesInstantsAsIso8601Strings() throws Exception {
		TopicAgitSyncEvent event = new TopicAgitSyncEvent(
				UUID.fromString("018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e"),
				"0190abcd-1111-7abc-def0-123456789abc",
				Instant.parse("2026-08-18T00:00:00Z"),
				Instant.parse("2026-08-18T17:00:00Z")
		);

		String json = objectMapper.writeValueAsString(event);

		assertThat(json).contains("\"startedAt\":\"2026-08-18T00:00:00Z\"");
		assertThat(json).contains("\"occurredAt\":\"2026-08-18T17:00:00Z\"");
		assertThat(json).doesNotContain("[2026,8,18");
	}
}
