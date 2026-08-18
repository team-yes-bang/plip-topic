package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.in.web.dto.CreateTopicRequest;
import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.adapter.in.web.dto.UpdateTopicRequest;
import com.plip.topic.adapter.in.web.mapper.TopicWebMapper;
import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Topic", description = "토픽 API")
@RequestMapping("/api/v1/topics")
@RestController
@RequiredArgsConstructor
public class TopicController {

	private final ListTopicsUseCase listTopicsUseCase;
	private final CreateTopicUseCase createTopicUseCase;
	private final UpdateTopicUseCase updateTopicUseCase;
	private final TopicWebMapper topicWebMapper;

	@Operation(summary = "토픽 생성", description = "아지트에 토픽을 생성합니다. 진행일이 없으면 오늘 00:00을 사용합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TopicResponseDto create(@RequestBody CreateTopicRequest request) {
		return topicWebMapper.toDto(createTopicUseCase.create(topicWebMapper.toDto(request)));
	}

	@Operation(summary = "토픽 수정", description = "토픽의 제목, 진행일, 레이아웃을 수정합니다. 전달하지 않은 필드는 유지됩니다.")
	@PatchMapping("/{topicUuid}")
	public TopicResponseDto update(
			@Parameter(description = "토픽 UUID", required = true)
			@PathVariable UUID topicUuid,
			@RequestBody UpdateTopicRequest request
	) {
		return topicWebMapper.toDto(updateTopicUseCase.update(topicUuid, topicWebMapper.toDto(request)));
	}

	@Operation(summary = "토픽 목록 조회", description = "아지트에 속한 삭제되지 않은 토픽을 진행일 내림차순으로 조회합니다.")
	@GetMapping
	public List<TopicResponseDto> list(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid
	) {
		return topicWebMapper.toDtoList(listTopicsUseCase.listByAgitUuid(agitUuid));
	}
}
