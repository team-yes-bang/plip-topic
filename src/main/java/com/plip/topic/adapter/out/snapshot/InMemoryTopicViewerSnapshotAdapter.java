package com.plip.topic.adapter.out.snapshot;

import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemoryTopicViewerSnapshotAdapter implements TopicViewerSnapshotPort {

	private final ConcurrentHashMap<UUID, Topic> snapshots = new ConcurrentHashMap<>();

	@Override
	public Optional<Topic> findByTopicUuid(UUID topicUuid) {
		if (topicUuid == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(snapshots.get(topicUuid))
				.filter(topic -> !topic.isDeleted());
	}

	@Override
	public void save(Topic topic) {
		if (topic == null || topic.getTopicUuid() == null) {
			return;
		}
		if (topic.isDeleted()) {
			snapshots.remove(topic.getTopicUuid());
			return;
		}
		snapshots.put(topic.getTopicUuid(), topic);
	}

	@Override
	public void delete(UUID topicUuid) {
		if (topicUuid == null) {
			return;
		}
		snapshots.remove(topicUuid);
	}
}
