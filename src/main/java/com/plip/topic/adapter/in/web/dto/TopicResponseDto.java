package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "토픽 응답")
public class TopicResponseDto {

	@Schema(description = "토픽 UUID")
	private UUID topicUuid;

	@Schema(description = "아지트 UUID")
	private UUID agitUuid;

	@Schema(description = "생성자 UUID")
	private UUID creatorUuid;

	@Schema(description = "토픽 제목")
	private String title;

	@Schema(description = "진행 날짜")
	private LocalDateTime startAt;

	@Schema(description = "연결된 비디오 UUID 목록")
	private List<UUID> videoUuids;

	@Schema(description = "생성 시각")
	private LocalDateTime createdAt;
}
