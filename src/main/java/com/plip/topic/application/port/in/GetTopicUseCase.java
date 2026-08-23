package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicResult;

import java.util.UUID;

public interface GetTopicUseCase {

	TopicResult get(UUID topicUuid, UUID actorUuid);
}
