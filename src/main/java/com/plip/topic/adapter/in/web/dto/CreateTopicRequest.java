package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "토픽 생성 요청. 주제만 만든다. 영상은 POST /topics/{topicUuid}/videos")
public class CreateTopicRequest {

	@Schema(description = "아지트 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID agitUuid;

	@Schema(description = "토픽 제목", example = "점심 메뉴")
	private String title;

	@Schema(description = "진행 날짜. 없으면 오늘 00:00")
	private LocalDateTime startAt;
}
