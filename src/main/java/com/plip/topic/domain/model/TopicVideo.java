package com.plip.topic.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicVideo {

	private UUID videoUuid;
	private LocalDateTime createdAt;
	private LocalDateTime deletedAt;

	public static TopicVideo create(UUID videoUuid) {
		if (videoUuid == null) {
			throw new IllegalArgumentException("videoUuid는 필수입니다.");
		}
		return TopicVideo.builder()
				.videoUuid(videoUuid)
				.build();
	}

	public static TopicVideo reconstitute(
			UUID videoUuid,
			LocalDateTime createdAt,
			LocalDateTime deletedAt
	) {
		return TopicVideo.builder()
				.videoUuid(videoUuid)
				.createdAt(createdAt)
				.deletedAt(deletedAt)
				.build();
	}

	boolean isDeleted() {
		return deletedAt != null;
	}

	@Builder(access = AccessLevel.PRIVATE)
	private TopicVideo(UUID videoUuid, LocalDateTime createdAt, LocalDateTime deletedAt) {
		this.videoUuid = videoUuid;
		this.createdAt = createdAt;
		this.deletedAt = deletedAt;
	}
}
