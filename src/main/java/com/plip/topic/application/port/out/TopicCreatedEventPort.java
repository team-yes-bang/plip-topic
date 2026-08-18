package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;

public interface TopicCreatedEventPort {

	void publishCreated(Topic topic);
}
