package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.in.web.dto.CreateTopicRequest;
import com.plip.topic.adapter.in.web.dto.TopicCalendarResponse;
import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.adapter.in.web.dto.UpdateTopicRequest;
import com.plip.topic.adapter.in.web.mapper.TopicWebMapper;
import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.DeleteTopicUseCase;
import com.plip.topic.application.port.in.GetTopicCalendarUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Tag(name = "Topic", description = "토픽 API. 조회는 아지트+날짜/월만 지원한다.")
@RequestMapping("/api/v1/topics")
@RestController
@RequiredArgsConstructor
public class TopicController {

	private final ListTopicsUseCase listTopicsUseCase;
	private final GetTopicCalendarUseCase getTopicCalendarUseCase;
	private final CreateTopicUseCase createTopicUseCase;
	private final UpdateTopicUseCase updateTopicUseCase;
	private final DeleteTopicUseCase deleteTopicUseCase;
	private final TopicWebMapper topicWebMapper;

	@Operation(summary = "토픽 생성", description = "아지트에 토픽을 생성합니다. 진행일이 없으면 오늘 00:00을 사용합니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TopicResponseDto create(@RequestBody CreateTopicRequest request) {
		return topicWebMapper.toDto(createTopicUseCase.create(topicWebMapper.toDto(request)));
	}

	@Operation(summary = "토픽 수정", description = "토픽의 제목, 진행일을 수정합니다. 전달하지 않은 필드는 유지됩니다.")
	@PatchMapping("/{topicUuid}")
	public TopicResponseDto update(
			@Parameter(description = "토픽 UUID", required = true)
			@PathVariable UUID topicUuid,
			@RequestBody UpdateTopicRequest request
	) {
		return topicWebMapper.toDto(updateTopicUseCase.update(topicUuid, topicWebMapper.toDto(request)));
	}

	@Operation(summary = "토픽 삭제", description = "영상이 없는 토픽만 소프트 삭제합니다.")
	@DeleteMapping("/{topicUuid}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@Parameter(description = "토픽 UUID", required = true)
			@PathVariable UUID topicUuid
	) {
		deleteTopicUseCase.delete(topicUuid);
	}

	@Operation(summary = "토픽 목록 조회", description = "아지트의 해당 날짜 토픽만 조회합니다. 전체 기간 조회는 제공하지 않습니다.")
	@GetMapping
	public List<TopicResponseDto> list(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid,
			@Parameter(description = "조회 날짜 (yyyy-MM-dd)", required = true)
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		return topicWebMapper.toDtoList(listTopicsUseCase.listByAgitUuidAndDate(agitUuid, date));
	}

	@Operation(
			summary = "토픽 캘린더 조회",
			description = "해당 연월에서 영상이 있는 날짜만 반환합니다. 아지트 제목/멤버십은 아지트 서비스 영역입니다."
	)
	@GetMapping("/calendar")
	public TopicCalendarResponse calendar(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid,
			@Parameter(description = "연월 (yyyy-MM)", required = true, example = "2026-08")
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
	) {
		return topicWebMapper.toCalendarDto(getTopicCalendarUseCase.getCalendar(agitUuid, yearMonth));
	}
}
