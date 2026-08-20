package com.plip.topic.adapter.out.snapshot.document;

import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = TopicViewerDocument.COLLECTION)
public class TopicViewerDocument {

	public static final String COLLECTION = "topic_viewer";

	@Id
	private String id;

	private String topicUuid;
	@Indexed
	private String agitUuid;
	private String creatorUuid;
	private String title;
	private LocalDateTime startAt;
	private int videoCount;
	private List<TopicViewerVideoDocument> videos = new ArrayList<>();
	private LocalDateTime createdAt;
	private boolean deleted;
	private Instant projectedAt;

	public static TopicViewerDocument from(Topic topic) {
		TopicViewerDocument document = new TopicViewerDocument();
		String topicUuid = topic.getTopicUuid().toString();
		document.setId(topicUuid);
		document.setTopicUuid(topicUuid);
		document.setAgitUuid(topic.getAgitUuid().toString());
		document.setCreatorUuid(topic.getCreatorUuid().toString());
		document.setTitle(topic.getTitle());
		document.setStartAt(topic.getStartAt());
		document.setVideoCount(topic.videoCount());
		document.setVideos(topic.getVideos().stream().map(TopicViewerVideoDocument::from).toList());
		document.setCreatedAt(topic.getCreatedAt());
		document.setDeleted(topic.isDeleted());
		document.setProjectedAt(Instant.now());
		return document;
	}

	public Topic toDomain() {
		List<TopicVideo> domainVideos = videos == null
				? List.of()
				: videos.stream().map(TopicViewerVideoDocument::toDomain).toList();
		return Topic.reconstitute(
				UUID.fromString(topicUuid),
				UUID.fromString(agitUuid),
				UUID.fromString(creatorUuid),
				title,
				startAt,
				domainVideos,
				createdAt,
				null,
				deleted ? LocalDateTime.MIN : null
		);
	}
}
