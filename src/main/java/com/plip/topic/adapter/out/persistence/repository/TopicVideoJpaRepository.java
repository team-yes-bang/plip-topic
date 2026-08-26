package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TopicVideoJpaRepository extends JpaRepository<TopicVideoEntity, Long> {

	@Query("SELECT DISTINCT tv.topic.agitUuid FROM TopicVideoEntity tv WHERE tv.videoUuid = :videoUuid AND tv.deletedAt IS NULL AND tv.topic.deletedAt IS NULL")
	List<UUID> findAgitUuidsByVideoUuid(@Param("videoUuid") UUID videoUuid);
}
