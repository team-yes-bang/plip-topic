package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicVideoResult;

import java.util.List;
import java.util.UUID;

public interface ListTopicVideosUseCase {

	List<TopicVideoResult> list(UUID topicUuid, UUID actorUuid);
}
