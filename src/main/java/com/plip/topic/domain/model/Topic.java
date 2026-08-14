package com.plip.topic.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

	private UUID topicUuid;
	private UUID agitUuid;
	private UUID creatorUuid;
	private String title;
	private LocalDateTime startAt;
	private String layout;
	private List<TopicVideo> videos;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public static Topic create(
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
			String layout,
			List<UUID> videoUuids
	) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (creatorUuid == null) {
			throw new IllegalArgumentException("creatorUuid는 필수입니다.");
		}
		validateTitle(title);
		validateLayout(layout);

		return Topic.builder()
				.topicUuid(UuidV7.create())
				.agitUuid(agitUuid)
				.creatorUuid(creatorUuid)
				.title(title)
				.startAt(startAt != null ? startAt : LocalDate.now().atStartOfDay())
				.layout(layout)
				.videos(toVideos(videoUuids))
				.build();
	}

	public static Topic reconstitute(
			UUID topicUuid,
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
			String layout,
			List<TopicVideo> videos,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		return Topic.builder()
				.topicUuid(topicUuid)
				.agitUuid(agitUuid)
				.creatorUuid(creatorUuid)
				.title(title)
				.startAt(startAt)
				.layout(layout)
				.videos(videos)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.deletedAt(deletedAt)
				.build();
	}

	public Topic softDelete(LocalDateTime deletedAt) {
		if (deletedAt == null) {
			throw new IllegalArgumentException("deletedAt은 필수입니다.");
		}
		return Topic.builder()
				.topicUuid(this.topicUuid)
				.agitUuid(this.agitUuid)
				.creatorUuid(this.creatorUuid)
				.title(this.title)
				.startAt(this.startAt)
				.layout(this.layout)
				.videos(this.videos)
				.createdAt(this.createdAt)
				.updatedAt(this.updatedAt)
				.deletedAt(deletedAt)
				.build();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	private static List<TopicVideo> toVideos(List<UUID> videoUuids) {
		if (videoUuids == null || videoUuids.isEmpty()) {
			return List.of();
		}
		Set<UUID> unique = new LinkedHashSet<>();
		List<TopicVideo> videos = new ArrayList<>();
		for (UUID videoUuid : videoUuids) {
			if (videoUuid == null) {
				throw new IllegalArgumentException("videoUuid는 필수입니다.");
			}
			if (!unique.add(videoUuid)) {
				throw new IllegalArgumentException("중복된 videoUuid는 허용되지 않습니다.");
			}
			videos.add(TopicVideo.create(videoUuid));
		}
		return List.copyOf(videos);
	}

	private static void validateTitle(String title) {
		if (title != null && title.length() > 255) {
			throw new IllegalArgumentException("title은 255자 이하여야 합니다.");
		}
	}

	private static void validateLayout(String layout) {
		if (layout != null && layout.length() > 50) {
			throw new IllegalArgumentException("layout은 50자 이하여야 합니다.");
		}
	}

	@Builder(access = AccessLevel.PRIVATE)
	private Topic(
			UUID topicUuid,
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
			String layout,
			List<TopicVideo> videos,
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		this.topicUuid = topicUuid;
		this.agitUuid = agitUuid;
		this.creatorUuid = creatorUuid;
		this.title = title;
		this.startAt = startAt;
		this.layout = layout;
		this.videos = videos == null ? List.of() : List.copyOf(videos);
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
