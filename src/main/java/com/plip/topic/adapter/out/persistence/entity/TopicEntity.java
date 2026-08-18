package com.plip.topic.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(
		name = "topic",
		uniqueConstraints = @UniqueConstraint(name = "uk_topic_uuid", columnNames = "topic_uuid"),
		indexes = @Index(
				name = "idx_topic_agit_deleted_start",
				columnList = "agit_uuid, deleted_at, start_at"
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "topic_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID topicUuid;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "agit_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID agitUuid;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "creator_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID creatorUuid;

	@Column(name = "title", length = 255)
	private String title;

	@Column(name = "start_at")
	private LocalDateTime startAt;

	@Column(name = "layout", length = 50)
	private String layout;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TopicVideoEntity> videos = new ArrayList<>();

	@Builder
	private TopicEntity(
			UUID topicUuid,
			UUID agitUuid,
			UUID creatorUuid,
			String title,
			LocalDateTime startAt,
			String layout
	) {
		this.topicUuid = topicUuid;
		this.agitUuid = agitUuid;
		this.creatorUuid = creatorUuid;
		this.title = title;
		this.startAt = startAt;
		this.layout = layout;
		this.videos = new ArrayList<>();
	}

	public void addVideo(UUID videoUuid) {
		this.videos.add(TopicVideoEntity.builder()
				.topic(this)
				.videoUuid(videoUuid)
				.build());
	}

	public void update(String title, LocalDateTime startAt, String layout) {
		this.title = title;
		this.startAt = startAt;
		this.layout = layout;
	}

	public void softDelete(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
		this.videos.forEach(video -> video.softDelete(deletedAt));
	}
}
