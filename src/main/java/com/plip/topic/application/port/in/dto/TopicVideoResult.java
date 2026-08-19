package com.plip.topic.application.port.in.dto;

import com.plip.topic.domain.model.TopicVideo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TopicVideoResult {

	private UUID videoUuid;
	private UUID userUuid;
	private LocalDateTime createdAt;

	public static TopicVideoResult from(TopicVideo video) {
		return TopicVideoResult.builder()
				.videoUuid(video.getVideoUuid())
				.userUuid(video.getUserUuid())
				.createdAt(video.getCreatedAt())
				.build();
	}
}
