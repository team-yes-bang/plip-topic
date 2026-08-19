package com.plip.topic.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
		name = "topic_video",
		uniqueConstraints = @UniqueConstraint(name = "uk_topic_video", columnNames = {"topic_id", "video_uuid"})
)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TopicVideoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "topic_id", nullable = false)
	private TopicEntity topic;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "video_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID videoUuid;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "user_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID userUuid;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private TopicVideoEntity(TopicEntity topic, UUID videoUuid, UUID userUuid) {
		this.topic = topic;
		this.videoUuid = videoUuid;
		this.userUuid = userUuid;
	}

	public void softDelete(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}
}
