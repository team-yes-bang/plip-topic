package com.plip.topic.adapter.out.persistence.mapper;

import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import com.plip.topic.adapter.out.persistence.entity.TopicVideoEntity;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TopicPersistenceMapper {

	public TopicEntity toEntity(Topic topic) {
		TopicEntity entity = TopicEntity.builder()
				.topicUuid(topic.getTopicUuid())
				.agitUuid(topic.getAgitUuid())
				.creatorUuid(topic.getCreatorUuid())
				.title(topic.getTitle())
				.startAt(topic.getStartAt())
				.build();
		topic.getVideos().forEach(video -> entity.addVideo(video.getVideoUuid()));
		return entity;
	}

	public Topic toDomain(TopicEntity entity) {
		List<TopicVideoEntity> videoEntities = entity.getVideos() == null ? List.of() : entity.getVideos();
		List<TopicVideo> videos = videoEntities.stream()
				.filter(video -> video.getDeletedAt() == null)
				.map(video -> TopicVideo.reconstitute(
						video.getVideoUuid(),
						video.getCreatedAt(),
						video.getDeletedAt()
				))
				.toList();
		return Topic.reconstitute(
				entity.getTopicUuid(),
				entity.getAgitUuid(),
				entity.getCreatorUuid(),
				entity.getTitle(),
				entity.getStartAt(),
				videos,
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
