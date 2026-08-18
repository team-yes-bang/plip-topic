package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "토픽 생성 요청")
public class CreateTopicRequest {

	@Schema(description = "아지트 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID agitUuid;

	@Schema(description = "생성자 UUID (임시 body 전달 — 추후 인증에서 추출)", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID creatorUuid;

	@Schema(description = "토픽 제목", example = "점심 메뉴")
	private String title;

	@Schema(description = "진행 날짜. 없으면 오늘 00:00")
	private LocalDateTime startAt;

	@Schema(description = "그룹 영상 뷰어 레이아웃", example = "grid")
	private String layout;

	@Schema(description = "연결할 비디오 UUID 목록")
	private List<UUID> videoUuids;
}
