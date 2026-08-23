package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "토픽(주제) 응답. 영상 격자는 GET /topics/{topicUuid}/videos")
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

	@Schema(description = "붙은 영상 수")
	private int videoCount;

	@Schema(description = "조회한 사용자가 이 토픽에 영상을 올렸는지")
	private Boolean uploadedByMe;

	@Schema(description = "생성 시각")
	private LocalDateTime createdAt;
}
