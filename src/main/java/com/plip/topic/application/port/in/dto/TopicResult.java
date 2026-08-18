package com.plip.topic.application.port.in.dto;

import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TopicResult {

	private UUID topicUuid;
	private UUID agitUuid;
	private UUID creatorUuid;
	private String title;
	private LocalDateTime startAt;
	private List<UUID> videoUuids;
	private LocalDateTime createdAt;

	public static TopicResult from(Topic topic) {
		return TopicResult.builder()
				.topicUuid(topic.getTopicUuid())
				.agitUuid(topic.getAgitUuid())
				.creatorUuid(topic.getCreatorUuid())
				.title(topic.getTitle())
				.startAt(topic.getStartAt())
				.videoUuids(topic.getVideos().stream().map(TopicVideo::getVideoUuid).toList())
				.createdAt(topic.getCreatedAt())
				.build();
	}
}
