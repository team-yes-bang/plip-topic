package com.plip.topic.adapter.out.kafka;

import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.domain.model.Topic;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpTopicAgitSyncEventAdapter implements TopicAgitSyncEventPort {

	@Override
	public void publishBoundAndStarted(Topic topic) {
	}

	@Override
	public void publishUnbound(Topic topic) {
	}
}
