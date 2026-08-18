package com.plip.topic.adapter.out.kafka;

import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.domain.model.Topic;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpTopicCreatedEventAdapter implements TopicCreatedEventPort {

	@Override
	public void publishCreated(Topic topic) {
	}
}
