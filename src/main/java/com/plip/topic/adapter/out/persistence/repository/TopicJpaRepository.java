package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicJpaRepository extends JpaRepository<TopicEntity, Long> {

	@EntityGraph(attributePaths = "videos")
	Optional<TopicEntity> findByTopicUuidAndDeletedAtIsNull(UUID topicUuid);

	@EntityGraph(attributePaths = "videos")
	List<TopicEntity> findAllByAgitUuidAndDeletedAtIsNullOrderByStartAtDesc(UUID agitUuid);
}
