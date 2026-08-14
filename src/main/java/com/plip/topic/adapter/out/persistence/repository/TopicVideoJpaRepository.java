package com.plip.topic.adapter.out.persistence.repository;

import com.plip.topic.adapter.out.persistence.entity.TopicVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicVideoJpaRepository extends JpaRepository<TopicVideoEntity, Long> {
}
