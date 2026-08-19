package com.plip.topic.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

	private UUID topicUuid;
	private UUID agitUuid;
	private UUID creatorUuid;
	private String title;
	private LocalDateTime startAt;
	private List<TopicVideo> videos;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public static Topic create(
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt
	) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (creatorUuid == null) {
			throw new IllegalArgumentException("creatorUuid는 필수입니다.");
		}
		validateTitle(title);

		return Topic.builder()
				.topicUuid(UuidV7.create())
				.agitUuid(agitUuid)
				.creatorUuid(creatorUuid)
				.title(title)
				.startAt(startAt != null ? startAt : LocalDate.now().atStartOfDay())
				.videos(List.of())
				.build();
	}

	public static Topic reconstitute(
			UUID topicUuid,
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
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
				.videos(videos)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.deletedAt(deletedAt)
				.build();
	}

	public Topic update(String title, LocalDateTime startAt) {
		if (isDeleted()) {
			throw new IllegalArgumentException("삭제된 토픽은 수정할 수 없습니다.");
		}
		String nextTitle = title != null ? title : this.title;
		LocalDateTime nextStartAt = startAt != null ? startAt : this.startAt;
		validateTitle(nextTitle);
		return copyWith(nextTitle, nextStartAt, this.videos, this.deletedAt);
	}

	public Topic attachVideo(UUID userUuid, UUID videoUuid) {
		if (isDeleted()) {
			throw new IllegalArgumentException("삭제된 토픽에는 영상을 붙일 수 없습니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		if (videoUuid == null) {
			throw new IllegalArgumentException("videoUuid는 필수입니다.");
		}
		if (videos.stream().anyMatch(video -> videoUuid.equals(video.getVideoUuid()))) {
			return this;
		}
		if (videos.stream().anyMatch(video -> video.isOwnedBy(userUuid))) {
			throw new TopicVideoLimitException();
		}
		List<TopicVideo> next = new ArrayList<>(videos);
		next.add(TopicVideo.create(videoUuid, userUuid));
		return copyWith(this.title, this.startAt, List.copyOf(next), this.deletedAt);
	}

	public Topic detachVideo(UUID userUuid, UUID videoUuid) {
		if (isDeleted()) {
			throw new IllegalArgumentException("삭제된 토픽의 영상은 제거할 수 없습니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		if (videoUuid == null) {
			throw new IllegalArgumentException("videoUuid는 필수입니다.");
		}
		TopicVideo target = videos.stream()
				.filter(video -> videoUuid.equals(video.getVideoUuid()))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("영상이 존재하지 않습니다."));
		if (!target.isOwnedBy(userUuid)) {
			throw new IllegalArgumentException("본인 영상만 제거할 수 있습니다.");
		}
		List<TopicVideo> next = videos.stream()
				.filter(video -> !videoUuid.equals(video.getVideoUuid()))
				.toList();
		return copyWith(this.title, this.startAt, next, this.deletedAt);
	}

	public void assertDeletable() {
		if (isDeleted()) {
			throw new IllegalArgumentException("삭제된 토픽은 다시 삭제할 수 없습니다.");
		}
		if (!videos.isEmpty()) {
			throw new IllegalArgumentException("영상이 있는 토픽은 삭제할 수 없습니다.");
		}
	}

	public Topic softDelete(LocalDateTime deletedAt) {
		if (deletedAt == null) {
			throw new IllegalArgumentException("deletedAt은 필수입니다.");
		}
		return copyWith(this.title, this.startAt, this.videos, deletedAt);
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public int videoCount() {
		return videos.size();
	}

	public boolean uploadedBy(UUID userUuid) {
		if (userUuid == null) {
			return false;
		}
		return videos.stream().anyMatch(video -> video.isOwnedBy(userUuid));
	}

	private Topic copyWith(String title, LocalDateTime startAt, List<TopicVideo> videos, LocalDateTime deletedAt) {
		return Topic.builder()
				.topicUuid(this.topicUuid)
				.agitUuid(this.agitUuid)
				.creatorUuid(this.creatorUuid)
				.title(title)
				.startAt(startAt)
				.videos(videos)
				.createdAt(this.createdAt)
				.updatedAt(this.updatedAt)
				.deletedAt(deletedAt)
				.build();
	}

	private static void validateTitle(String title) {
		if (title != null && title.length() > 255) {
			throw new IllegalArgumentException("title은 255자 이하여야 합니다.");
		}
	}

	@Builder(access = AccessLevel.PRIVATE)
	private Topic(
			UUID topicUuid,
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
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
		this.videos = videos == null ? List.of() : List.copyOf(videos);
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}
}
