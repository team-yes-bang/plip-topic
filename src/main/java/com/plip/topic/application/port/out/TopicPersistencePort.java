package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicPersistencePort {

	Topic save(Topic topic);

	Optional<Topic> findByTopicUuid(UUID topicUuid);

	List<Topic> findAllByAgitUuid(UUID agitUuid);

	void deleteByTopicUuid(UUID topicUuid);
}
