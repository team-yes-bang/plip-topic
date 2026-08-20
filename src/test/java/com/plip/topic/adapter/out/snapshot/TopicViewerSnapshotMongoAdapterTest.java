package com.plip.topic.adapter.out.snapshot;

import com.plip.topic.adapter.out.snapshot.document.TopicViewerDocument;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicViewerSnapshotMongoAdapterTest {

	@Mock
	private MongoTemplate mongoTemplate;

	private TopicViewerSnapshotMongoAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new TopicViewerSnapshotMongoAdapter(mongoTemplate);
	}

	@Test
	void save_upsertsViewerDocument() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "점심 메뉴", LocalDateTime.of(2026, 8, 18, 0, 0));

		adapter.save(topic);

		ArgumentCaptor<TopicViewerDocument> captor = ArgumentCaptor.forClass(TopicViewerDocument.class);
		verify(mongoTemplate).save(captor.capture());
		TopicViewerDocument document = captor.getValue();
		assertThat(document.getId()).isEqualTo(topic.getTopicUuid().toString());
		assertThat(document.getTitle()).isEqualTo("점심 메뉴");
		assertThat(document.getVideoCount()).isZero();
		assertThat(document.isDeleted()).isFalse();
	}

	@Test
	void findByTopicUuid_mapsDocumentWhenPresent() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "점심 메뉴", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(mongoTemplate.findOne(any(Query.class), eq(TopicViewerDocument.class)))
				.willReturn(TopicViewerDocument.from(topic));

		Topic found = adapter.findByTopicUuid(topic.getTopicUuid()).orElseThrow();

		assertThat(found.getTopicUuid()).isEqualTo(topic.getTopicUuid());
		assertThat(found.getTitle()).isEqualTo("점심 메뉴");
	}

	@Test
	void delete_removesByTopicUuid() {
		UUID topicUuid = UUID.randomUUID();

		adapter.delete(topicUuid);

		verify(mongoTemplate).remove(any(Query.class), eq(TopicViewerDocument.class));
	}

	@Test
	void save_ignoresNullTopic() {
		adapter.save(null);
		verify(mongoTemplate, never()).save(any());
	}
}
