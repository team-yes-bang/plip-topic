package com.plip.topic.adapter.out.persistence;

import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import com.plip.topic.adapter.out.persistence.mapper.TopicPersistenceMapper;
import com.plip.topic.adapter.out.persistence.repository.TopicJpaRepository;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicPersistenceAdapter implements TopicPersistencePort {

	private final TopicJpaRepository topicJpaRepository;
	private final TopicPersistenceMapper topicPersistenceMapper;

	@Override
	@Transactional
	public Topic save(Topic topic) {
		TopicEntity saved = topicJpaRepository.save(topicPersistenceMapper.toEntity(topic));
		return topicPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Topic> findByTopicUuid(UUID topicUuid) {
		return topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.map(topicPersistenceMapper::toDomain);
	}

	@Override
	public List<Topic> findAllByAgitUuid(UUID agitUuid) {
		return topicJpaRepository.findAllByAgitUuidAndDeletedAtIsNullOrderByStartAtDesc(agitUuid)
				.stream()
				.map(topicPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	@Transactional
	public Topic update(Topic topic) {
		TopicEntity entity = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topic.getTopicUuid())
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		entity.update(topic.getTitle(), topic.getStartAt());
		return topicPersistenceMapper.toDomain(entity);
	}

	@Override
	@Transactional
	public void deleteByTopicUuid(UUID topicUuid) {
		TopicEntity entity = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		entity.softDelete(LocalDateTime.now());
	}

	@Override
	@Transactional
	public boolean addVideoIfAbsent(UUID topicUuid, UUID videoUuid) {
		return topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.map(entity -> {
					boolean alreadyAttached = entity.getVideos().stream()
							.anyMatch(video -> videoUuid.equals(video.getVideoUuid()));
					if (alreadyAttached) {
						return false;
					}
					entity.addVideo(videoUuid);
					return true;
				})
				.orElse(false);
	}
}
