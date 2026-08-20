package com.plip.topic.adapter.out.snapshot;

import com.plip.topic.adapter.out.snapshot.document.TopicViewerDocument;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class TopicViewerSnapshotMongoAdapter implements TopicViewerSnapshotPort {

	private final MongoTemplate mongoTemplate;

	@Override
	public Optional<Topic> findByTopicUuid(UUID topicUuid) {
		if (topicUuid == null) {
			return Optional.empty();
		}
		Query query = Query.query(Criteria.where("_id").is(topicUuid.toString()).and("deleted").ne(true));
		TopicViewerDocument document = mongoTemplate.findOne(query, TopicViewerDocument.class);
		if (document == null) {
			return Optional.empty();
		}
		return Optional.of(document.toDomain());
	}

	@Override
	public void save(Topic topic) {
		if (topic == null || topic.getTopicUuid() == null) {
			return;
		}
		if (topic.isDeleted()) {
			delete(topic.getTopicUuid());
			return;
		}
		mongoTemplate.save(TopicViewerDocument.from(topic));
	}

	@Override
	public void delete(UUID topicUuid) {
		if (topicUuid == null) {
			return;
		}
		mongoTemplate.remove(
				Query.query(Criteria.where("_id").is(topicUuid.toString())),
				TopicViewerDocument.class
		);
	}
}
