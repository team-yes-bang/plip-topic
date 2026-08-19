package com.plip.topic.application.port.in;

import java.util.UUID;

public interface DetachTopicVideoUseCase {

	void detach(UUID topicUuid, UUID videoUuid, UUID userUuid);
}
