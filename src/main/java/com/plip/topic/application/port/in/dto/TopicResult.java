package com.plip.topic.application.port.in.dto;

import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideo;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
public class TopicResult {

	private UUID topicUuid;
	private UUID agitUuid;
	private UUID creatorUuid;
	private String title;
	private LocalDateTime startAt;
	private int videoCount;
	private List<UUID> uploaderUuids;
	private LocalDateTime createdAt;

	public static TopicResult from(Topic topic) {
		List<UUID> uploaders = topic.getVideos().stream()
				.map(TopicVideo::getUserUuid)
				.toList();
		return TopicResult.builder()
				.topicUuid(topic.getTopicUuid())
				.agitUuid(topic.getAgitUuid())
				.creatorUuid(topic.getCreatorUuid())
				.title(topic.getTitle())
				.startAt(topic.getStartAt())
				.videoCount(topic.videoCount())
				.uploaderUuids(uploaders)
				.createdAt(topic.getCreatedAt())
				.build();
	}

	public boolean uploadedBy(UUID userUuid) {
		if (userUuid == null || uploaderUuids == null) {
			return false;
		}
		return uploaderUuids.contains(userUuid);
	}
}
