package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "기존 토픽에 영상 붙이기. 사용자당 토픽당 1개")
public class AttachTopicVideoRequest {

	@Schema(description = "비디오 UUID", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID videoUuid;

	@Schema(description = "업로더 UUID (임시 body — 추후 인증에서 추출)", requiredMode = Schema.RequiredMode.REQUIRED)
	private UUID userUuid;
}
