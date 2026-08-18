package com.plip.topic.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(
		name = "topic_calendar_day",
		uniqueConstraints = @UniqueConstraint(name = "uk_topic_calendar_agit_day", columnNames = {"agit_uuid", "calendar_day"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TopicCalendarDayEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "agit_uuid", nullable = false, columnDefinition = "BINARY(16)", length = 16)
	private UUID agitUuid;

	@Column(name = "calendar_day", nullable = false)
	private LocalDate day;

	@Column(name = "topic_count", nullable = false)
	private int topicCount;

	@Column(name = "video_count", nullable = false)
	private int videoCount;

	public static TopicCalendarDayEntity create(UUID agitUuid, LocalDate day) {
		TopicCalendarDayEntity entity = new TopicCalendarDayEntity();
		entity.agitUuid = agitUuid;
		entity.day = day;
		entity.topicCount = 0;
		entity.videoCount = 0;
		return entity;
	}

	public void apply(int topicDelta, int videoDelta) {
		this.topicCount = Math.max(0, this.topicCount + topicDelta);
		this.videoCount = Math.max(0, this.videoCount + videoDelta);
	}
}
