package com.plip.topic.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "토픽 뷰어 이웃. 영상 있는 토픽만, 오늘 다음 지난 순서. before/after는 현재에 가까운 순.")
public class TopicFeedResponseDto {

	@Schema(description = "기준 토픽. 피드에 없으면 null")
	private TopicResponseDto current;

	@Schema(description = "순서상 앞(오늘 쪽). 가장 가까운 항목이 먼저")
	private List<TopicResponseDto> before;

	@Schema(description = "순서상 뒤(지난 쪽). 가장 가까운 항목이 먼저")
	private List<TopicResponseDto> after;
}
