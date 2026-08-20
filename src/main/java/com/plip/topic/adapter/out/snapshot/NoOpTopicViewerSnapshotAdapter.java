package com.plip.topic.adapter.out.snapshot;

import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
public class NoOpTopicViewerSnapshotAdapter implements TopicViewerSnapshotPort {

	@Override
	public Optional<Topic> findByTopicUuid(UUID topicUuid) {
		return Optional.empty();
	}

	@Override
	public void save(Topic topic) {
	}

	@Override
	public void delete(UUID topicUuid) {
	}
}
