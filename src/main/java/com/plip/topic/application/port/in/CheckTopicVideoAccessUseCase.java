package com.plip.topic.application.port.in;

import java.util.UUID;

public interface CheckTopicVideoAccessUseCase {

	TopicVideoAccessStatus checkAccess(UUID videoUuid, UUID userUuid);
}
