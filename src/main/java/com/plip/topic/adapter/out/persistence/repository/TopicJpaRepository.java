package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	@EntityGraph(attributePaths = "videos")
	@Query("""
			SELECT DISTINCT t FROM TopicEntity t
			WHERE t.agitUuid = :agitUuid
			  AND t.deletedAt IS NULL
			  AND t.startAt >= :from
			  AND t.startAt < :to
			ORDER BY t.startAt DESC
			""")
	List<TopicEntity> findAllByAgitUuidAndStartAtRange(
			@Param("agitUuid") UUID agitUuid,
			@Param("from") java.time.LocalDateTime from,
			@Param("to") java.time.LocalDateTime to
	);
}
