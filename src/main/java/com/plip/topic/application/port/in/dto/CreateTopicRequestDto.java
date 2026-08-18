package com.plip.topic.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreateTopicRequestDto {

	private final UUID agitUuid;
	private final UUID creatorUuid;
	private final String title;
	private final LocalDateTime startAt;
	private final List<UUID> videoUuids;
}
