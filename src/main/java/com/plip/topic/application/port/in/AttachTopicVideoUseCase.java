package com.plip.topic.application.port.in;

import java.util.UUID;

public interface AttachTopicVideoUseCase {

	boolean attach(UUID topicUuid, UUID videoUuid);
}
