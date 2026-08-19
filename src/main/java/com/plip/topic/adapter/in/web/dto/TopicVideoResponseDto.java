package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "토픽에 붙은 영상")
public class TopicVideoResponseDto {

	@Schema(description = "비디오 UUID")
	private UUID videoUuid;

	@Schema(description = "업로더 UUID")
	private UUID userUuid;

	@Schema(description = "붙은 시각")
	private LocalDateTime createdAt;
}
