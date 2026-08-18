package com.plip.topic.application.port.in;

import java.util.UUID;

public interface DeleteTopicUseCase {

	void delete(UUID topicUuid);
}
