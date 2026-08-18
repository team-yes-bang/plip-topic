package com.plip.topic.application.service;

import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import com.plip.topic.application.port.out.TopicPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachTopicVideoService implements AttachTopicVideoUseCase {

	private final TopicPersistencePort topicPersistencePort;

	@Override
	@Transactional
	public boolean attach(UUID topicUuid, UUID videoUuid) {
		if (topicUuid == null || videoUuid == null) {
			return false;
		}
		return topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid);
	}
}
