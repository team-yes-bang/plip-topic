package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicJpaRepository extends JpaRepository<TopicEntity, Long> {

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.topicUuid = :topicUuid
			  AND t.deletedAt IS NULL
			""")
	Optional<TopicEntity> findByTopicUuidAndDeletedAtIsNull(@Param("topicUuid") UUID topicUuid);

	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			ORDER BY t.startAt DESC, t.createdAt DESC
			""")
	List<TopicEntity> findLatestByAgitUuid(
			@Param("agitUuid") UUID agitUuid,
			org.springframework.data.domain.Pageable pageable
	);

	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt >= :fromInclusive
			  AND t.startAt < :toExclusive
			ORDER BY t.startAt ASC, t.createdAt ASC
			""")
	List<TopicEntity> findOngoingByAgitUuid(
			@Param("agitUuid") UUID agitUuid,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive,
			org.springframework.data.domain.Pageable pageable
	);

	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt >= :fromInclusive
			ORDER BY t.startAt ASC, t.createdAt ASC
			""")
	List<TopicEntity> findUpcomingByAgitUuid(
			@Param("agitUuid") UUID agitUuid,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			org.springframework.data.domain.Pageable pageable
	);

	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt < :toExclusive
			ORDER BY t.startAt DESC, t.createdAt DESC
			""")
	List<TopicEntity> findPastByAgitUuid(
			@Param("agitUuid") UUID agitUuid,
			@Param("toExclusive") LocalDateTime toExclusive,
			org.springframework.data.domain.Pageable pageable
	);

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt >= :fromInclusive
			  AND t.startAt < :toExclusive
			ORDER BY t.startAt ASC, t.createdAt ASC, t.topicUuid ASC
			""")
	List<TopicEntity> findFeedOngoingWithVideos(
			@Param("agitUuid") UUID agitUuid,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive
	);

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt >= :fromInclusive
			  AND t.startAt < :toExclusive
			  AND SIZE(t.videos) > 0
			ORDER BY t.startAt DESC, t.createdAt DESC, t.topicUuid DESC
			""")
	List<TopicEntity> findFeedOnDatePastOrder(
			@Param("agitUuid") UUID agitUuid,
			@Param("fromInclusive") LocalDateTime fromInclusive,
			@Param("toExclusive") LocalDateTime toExclusive,
			org.springframework.data.domain.Pageable pageable
	);

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt < :toExclusive
			  AND SIZE(t.videos) > 0
			ORDER BY t.startAt DESC, t.createdAt DESC, t.topicUuid DESC
			""")
	List<TopicEntity> findFeedPastFromStart(
			@Param("agitUuid") UUID agitUuid,
			@Param("toExclusive") LocalDateTime toExclusive,
			org.springframework.data.domain.Pageable pageable
	);

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt < :toExclusive
			  AND SIZE(t.videos) > 0
			  AND (
			    t.startAt < :startAt
			    OR (t.startAt = :startAt AND t.createdAt < :createdAt)
			    OR (t.startAt = :startAt AND t.createdAt = :createdAt AND t.topicUuid < :topicUuid)
			  )
			ORDER BY t.startAt DESC, t.createdAt DESC, t.topicUuid DESC
			""")
	List<TopicEntity> findFeedPastOlderThan(
			@Param("agitUuid") UUID agitUuid,
			@Param("toExclusive") LocalDateTime toExclusive,
			@Param("startAt") LocalDateTime startAt,
			@Param("createdAt") LocalDateTime createdAt,
			@Param("topicUuid") UUID topicUuid,
			org.springframework.data.domain.Pageable pageable
	);

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt < :toExclusive
			  AND SIZE(t.videos) > 0
			  AND (
			    t.startAt > :startAt
			    OR (t.startAt = :startAt AND t.createdAt > :createdAt)
			    OR (t.startAt = :startAt AND t.createdAt = :createdAt AND t.topicUuid > :topicUuid)
			  )
			ORDER BY t.startAt ASC, t.createdAt ASC, t.topicUuid ASC
			""")
	List<TopicEntity> findFeedPastNewerThan(
			@Param("agitUuid") UUID agitUuid,
			@Param("toExclusive") LocalDateTime toExclusive,
			@Param("startAt") LocalDateTime startAt,
			@Param("createdAt") LocalDateTime createdAt,
			@Param("topicUuid") UUID topicUuid,
			org.springframework.data.domain.Pageable pageable
	);
}
