package com.plip.topic.application.port.out;

import java.util.UUID;

public interface TopicVideoEventPort {

	void publishAttached(UUID topicUuid, UUID agitUuid, UUID videoUuid, UUID userUuid);
}
