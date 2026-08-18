package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "토픽 캘린더. 영상이 있는 날짜만 활성이다. 아지트 제목/멤버는 아지트 서비스 스냅샷이며 이 응답에 포함하지 않는다.")
public class TopicCalendarResponse {

	@Schema(description = "아지트 UUID")
	private UUID agitUuid;

	@Schema(description = "조회 연월", example = "2026-08")
	private String yearMonth;

	@Schema(description = "영상이 있는 날짜")
	private List<LocalDate> activeDates;
}
