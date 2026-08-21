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
}
