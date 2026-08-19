package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;

public interface TopicAgitSyncEventPort {

	void publishBoundAndStarted(Topic topic);

	void publishUnbound(Topic topic);
}
