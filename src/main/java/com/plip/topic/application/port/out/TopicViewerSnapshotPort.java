package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;

import java.util.Optional;
import java.util.UUID;

public interface TopicViewerSnapshotPort {

	Optional<Topic> findByTopicUuid(UUID topicUuid);

	void save(Topic topic);

	void delete(UUID topicUuid);
}
