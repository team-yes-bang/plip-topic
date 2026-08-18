package com.plip.topic.application.service;

import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachTopicVideoService implements AttachTopicVideoUseCase {

	private final TopicPersistencePort topicPersistencePort;
	private final TopicReadCachePort topicReadCachePort;

	@Override
	@Transactional
	public boolean attach(UUID topicUuid, UUID videoUuid) {
		if (topicUuid == null || videoUuid == null) {
			return false;
		}
		boolean attached = topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid);
		if (attached) {
			topicPersistencePort.findByTopicUuid(topicUuid).ifPresent(topic ->
					topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate()));
		}
		return attached;
	}
}
