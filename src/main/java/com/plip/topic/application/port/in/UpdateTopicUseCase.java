package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;

import java.util.UUID;

public interface UpdateTopicUseCase {

	TopicResult update(UUID topicUuid, UpdateTopicRequestDto request, UUID actorUuid);
}
