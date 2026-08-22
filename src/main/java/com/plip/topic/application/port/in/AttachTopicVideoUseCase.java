package com.plip.topic.application.port.in;

import java.util.UUID;

public interface AttachTopicVideoUseCase {

	boolean tryAttach(UUID topicUuid, UUID videoUuid, UUID userUuid);

	boolean attachOrThrow(UUID topicUuid, UUID videoUuid, UUID actorUuid, String authorization);
}
