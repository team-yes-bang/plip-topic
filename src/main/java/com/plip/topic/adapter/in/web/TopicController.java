package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.adapter.in.web.mapper.TopicWebMapper;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Topic", description = "토픽 API")
@RequestMapping("/api/v1/topics")
@RestController
@RequiredArgsConstructor
public class TopicController {

	private final ListTopicsUseCase listTopicsUseCase;
	private final TopicWebMapper topicWebMapper;

	@Operation(summary = "토픽 목록 조회", description = "아지트에 속한 삭제되지 않은 토픽을 진행일 내림차순으로 조회합니다.")
	@GetMapping
	public List<TopicResponseDto> list(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid
	) {
		return topicWebMapper.toDtoList(listTopicsUseCase.listByAgitUuid(agitUuid));
	}
}
