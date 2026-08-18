package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "토픽 수정 요청. 전달하지 않은 필드는 유지됩니다.")
public class UpdateTopicRequest {

	@Schema(description = "토픽 제목", example = "저녁 메뉴")
	private String title;

	@Schema(description = "진행 날짜")
	private LocalDateTime startAt;
}
