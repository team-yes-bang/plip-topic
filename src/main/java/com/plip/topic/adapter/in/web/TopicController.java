package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.in.web.dto.AttachTopicVideoRequest;
import com.plip.topic.adapter.in.web.dto.CreateTopicRequest;
import com.plip.topic.adapter.in.web.dto.TopicCalendarResponse;
import com.plip.topic.adapter.in.web.dto.TopicFeedResponseDto;
import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.adapter.in.web.dto.TopicVideoResponseDto;
import com.plip.topic.adapter.in.web.dto.UpdateTopicRequest;
import com.plip.topic.adapter.in.web.mapper.TopicWebMapper;
import com.plip.topic.application.exception.UnauthenticatedActorException;
import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.DeleteTopicUseCase;
import com.plip.topic.application.port.in.DetachTopicVideoUseCase;
import com.plip.topic.application.port.in.GetTopicCalendarUseCase;
import com.plip.topic.application.port.in.GetTopicFeedUseCase;
import com.plip.topic.application.port.in.GetTopicUseCase;
import com.plip.topic.application.port.in.ListTopicVideosUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import com.plip.topic.domain.model.TopicListStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@Tag(name = "Topic", description = "토픽(주제) API. 영상 격자는 /videos 하위 리소스.")
@RequestMapping("/api/v1/topics")
@RestController
@RequiredArgsConstructor
public class TopicController {

	private final ListTopicsUseCase listTopicsUseCase;
	private final GetTopicUseCase getTopicUseCase;
	private final GetTopicFeedUseCase getTopicFeedUseCase;
	private final GetTopicCalendarUseCase getTopicCalendarUseCase;
	private final CreateTopicUseCase createTopicUseCase;
	private final UpdateTopicUseCase updateTopicUseCase;
	private final DeleteTopicUseCase deleteTopicUseCase;
	private final ListTopicVideosUseCase listTopicVideosUseCase;
	private final AttachTopicVideoUseCase attachTopicVideoUseCase;
	private final DetachTopicVideoUseCase detachTopicVideoUseCase;
	private final TopicWebMapper topicWebMapper;

	@Operation(summary = "토픽 생성", description = "아지트에 주제를 만듭니다. 생성자는 Access JWT이며 ACTIVE 멤버만 가능합니다. 영상은 붙이지 않습니다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TopicResponseDto create(@RequestBody CreateTopicRequest request, HttpServletRequest httpRequest) {
		UUID actorUuid = AuthenticatedActor.requireUserUuid();
		return topicWebMapper.toDto(
				createTopicUseCase.create(
						topicWebMapper.toDto(request),
						actorUuid,
						requireAuthorization(httpRequest)
				),
				actorUuid
		);
	}

	@Operation(
			summary = "토픽 뷰어 이웃 조회",
			description = "영상 있는 토픽만, 오늘 다음 지난 순서. topicUuid 또는 date 중 하나만. before/after 기본 1 최대 3."
	)
	@GetMapping("/feed")
	public TopicFeedResponseDto feed(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid,
			@Parameter(description = "기준 토픽 UUID. date와 함께 쓸 수 없음")
			@RequestParam(required = false) UUID topicUuid,
			@Parameter(description = "기준 날짜(KST yyyy-MM-dd). topicUuid와 함께 쓸 수 없음")
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@Parameter(description = "앞쪽 개수. 생략 시 1, 0~3")
			@RequestParam(required = false) Integer before,
			@Parameter(description = "뒤쪽 개수. 생략 시 1, 0~3")
			@RequestParam(required = false) Integer after
	) {
		return topicWebMapper.toFeedDto(
				getTopicFeedUseCase.feed(agitUuid, topicUuid, date, before, after),
				AuthenticatedActor.findUserUuid()
		);
	}

	@Operation(
			summary = "토픽 구간 목록 조회",
			description = "아지트의 토픽을 KST 날짜 기준 ONGOING/UPCOMING/PAST로 조회합니다. 최신 10개 갤러리는 GET /topics. actor가 있으면 uploadedByMe를 채웁니다."
	)
	@GetMapping("/list")
	public List<TopicResponseDto> listByStatus(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid,
			@Parameter(description = "KST 날짜 구간. ONGOING=오늘, UPCOMING=이후, PAST=이전", required = true)
			@RequestParam TopicListStatus status,
			@Parameter(description = "최대 개수. 생략 시 10, 1~20으로 제한")
			@RequestParam(required = false) Integer limit
	) {
		return topicWebMapper.toDtoList(
				listTopicsUseCase.listByAgitUuidAndStatus(agitUuid, status, limit),
				AuthenticatedActor.findUserUuid()
		);
	}

	@Operation(summary = "토픽 단건 조회")
	@GetMapping("/{topicUuid}")
	public TopicResponseDto get(@PathVariable UUID topicUuid) {
		return topicWebMapper.toDto(getTopicUseCase.get(topicUuid), AuthenticatedActor.findUserUuid());
	}

	@Operation(summary = "토픽 수정", description = "제목 또는 진행일. 전달하지 않은 필드는 유지됩니다. 생성자 또는 HOST만 가능합니다.")
	@PatchMapping("/{topicUuid}")
	public TopicResponseDto update(
			@Parameter(description = "토픽 UUID", required = true)
			@PathVariable UUID topicUuid,
			@RequestBody UpdateTopicRequest request,
			HttpServletRequest httpRequest
	) {
		UUID actorUuid = AuthenticatedActor.requireUserUuid();
		return topicWebMapper.toDto(
				updateTopicUseCase.update(
						topicUuid,
						topicWebMapper.toDto(request),
						actorUuid,
						requireAuthorization(httpRequest)
				),
				actorUuid
		);
	}

	@Operation(summary = "토픽 삭제", description = "영상이 없는 토픽만 소프트 삭제합니다. 생성자 또는 HOST만 가능합니다.")
	@DeleteMapping("/{topicUuid}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(
			@Parameter(description = "토픽 UUID", required = true)
			@PathVariable UUID topicUuid,
			HttpServletRequest httpRequest
	) {
		deleteTopicUseCase.delete(
				topicUuid,
				AuthenticatedActor.requireUserUuid(),
				requireAuthorization(httpRequest)
		);
	}

	@Operation(summary = "토픽 목록 조회", description = "아지트의 startAt 최신 토픽 최대 10개. 영상 격자는 GET /{topicUuid}/videos. actor가 있으면 uploadedByMe를 채웁니다.")
	@GetMapping
	public List<TopicResponseDto> list(
			@Parameter(description = "아지트 UUID", required = true)
			@RequestParam UUID agitUuid
	) {
		return topicWebMapper.toDtoList(
				listTopicsUseCase.listLatestByAgitUuid(agitUuid),
				AuthenticatedActor.findUserUuid()
		);
	}

	@Operation(
			summary = "토픽 캘린더 조회",
			description = "해당 연월에서 영상이 있는 날짜만 반환합니다."
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

	@Operation(summary = "토픽 영상 목록", description = "주제 상세 격자.")
	@GetMapping("/{topicUuid}/videos")
	public List<TopicVideoResponseDto> listVideos(@PathVariable UUID topicUuid) {
		return listTopicVideosUseCase.list(topicUuid).stream()
				.map(topicWebMapper::toVideoDto)
				.toList();
	}

	@Operation(summary = "토픽에 영상 붙이기", description = "사용자당 토픽당 1개. 같은 영상 재요청은 200. 업로더는 actor이며 ACTIVE 멤버만 가능합니다.")
	@PostMapping("/{topicUuid}/videos")
	public ResponseEntity<TopicVideoResponseDto> attachVideo(
			@PathVariable UUID topicUuid,
			@RequestBody AttachTopicVideoRequest request,
			HttpServletRequest httpRequest
	) {
		UUID actorUuid = AuthenticatedActor.requireUserUuid();
		String authorization = requireAuthorization(httpRequest);
		boolean created = attachTopicVideoUseCase.attachOrThrow(
				topicUuid,
				request.getVideoUuid(),
				actorUuid,
				authorization
		);
		TopicVideoResponseDto body = listTopicVideosUseCase.list(topicUuid).stream()
				.filter(video -> video.getVideoUuid().equals(request.getVideoUuid()))
				.findFirst()
				.map(topicWebMapper::toVideoDto)
				.orElseGet(() -> TopicVideoResponseDto.builder()
						.videoUuid(request.getVideoUuid())
						.userUuid(actorUuid)
						.build());
		return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).body(body);
	}

	@Operation(summary = "토픽에서 영상 제거", description = "본인 영상만 제거할 수 있습니다. 업로더는 actor입니다.")
	@DeleteMapping("/{topicUuid}/videos/{videoUuid}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void detachVideo(
			@PathVariable UUID topicUuid,
			@PathVariable UUID videoUuid
	) {
		detachTopicVideoUseCase.detach(topicUuid, videoUuid, AuthenticatedActor.requireUserUuid());
	}

	private static String requireAuthorization(HttpServletRequest httpRequest) {
		String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || authorization.isBlank()) {
			throw new UnauthenticatedActorException();
		}
		return authorization;
	}
}
