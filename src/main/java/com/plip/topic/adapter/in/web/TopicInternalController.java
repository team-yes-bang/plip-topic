package com.plip.topic.adapter.in.web;

import com.plip.topic.application.port.in.CheckTopicVideoAccessUseCase;
import com.plip.topic.application.port.in.TopicVideoAccessStatus;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Hidden
@Tag(name = "Topic Internal", description = "내부 서비스 간 인가/동기화 전용 API")
@RestController
@RequestMapping("/internal/v1/videos")
@RequiredArgsConstructor
public class TopicInternalController {

	private final CheckTopicVideoAccessUseCase checkTopicVideoAccessUseCase;

	@Operation(
			summary = "비디오 접근 권한 검증",
			description = "비디오 서비스에서 비소유자가 영상 재생/조회 시 토픽 소속 여부 및 아지트 멤버십을 검증합니다. Body 없이 HTTP 상태 코드만 반환합니다. (204: 승인, 403: 링크 없음 또는 비멤버, 401: 내부 API 키 없음/불일치)"
	)
	@RequestMapping(value = "/{videoUuid}/access/{userUuid}", method = RequestMethod.HEAD)
	public ResponseEntity<Void> checkAccess(
			@Parameter(description = "비디오 UUID", required = true)
			@PathVariable UUID videoUuid,
			@Parameter(description = "요청 사용자 UUID", required = true)
			@PathVariable UUID userUuid
	) {
		TopicVideoAccessStatus status = checkTopicVideoAccessUseCase.checkAccess(videoUuid, userUuid);
		return switch (status) {
			case ALLOWED -> ResponseEntity.noContent().build();
			case FORBIDDEN -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		};
	}
}
