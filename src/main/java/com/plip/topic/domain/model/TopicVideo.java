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
	private UUID userUuid;
	private LocalDateTime createdAt;
	private LocalDateTime deletedAt;

	public static TopicVideo create(UUID videoUuid, UUID userUuid) {
		if (videoUuid == null) {
			throw new IllegalArgumentException("videoUuid는 필수입니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		return TopicVideo.builder()
				.videoUuid(videoUuid)
				.userUuid(userUuid)
				.build();
	}

	public static TopicVideo reconstitute(
			UUID videoUuid,
			UUID userUuid,
			LocalDateTime createdAt,
			LocalDateTime deletedAt
	) {
		return TopicVideo.builder()
				.videoUuid(videoUuid)
				.userUuid(userUuid)
				.createdAt(createdAt)
				.deletedAt(deletedAt)
				.build();
	}

	boolean isDeleted() {
		return deletedAt != null;
	}

	boolean isOwnedBy(UUID userUuid) {
		return this.userUuid != null && this.userUuid.equals(userUuid);
	}

	@Builder(access = AccessLevel.PRIVATE)
	private TopicVideo(UUID videoUuid, UUID userUuid, LocalDateTime createdAt, LocalDateTime deletedAt) {
		this.videoUuid = videoUuid;
		this.userUuid = userUuid;
		this.createdAt = createdAt;
		this.deletedAt = deletedAt;
	}
}
