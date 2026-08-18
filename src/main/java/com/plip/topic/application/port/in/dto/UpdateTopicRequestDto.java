package com.plip.topic.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdateTopicRequestDto {

	private final String title;
	private final LocalDateTime startAt;
	private final String layout;
}
