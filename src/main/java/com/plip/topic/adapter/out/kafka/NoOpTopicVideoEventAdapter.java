package com.plip.topic.adapter.out.kafka;

import com.plip.topic.application.port.out.TopicVideoEventPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("test")
public class NoOpTopicVideoEventAdapter implements TopicVideoEventPort {

	@Override
	public void publishAttached(UUID topicUuid, UUID agitUuid, UUID videoUuid, UUID userUuid) {
		// 테스트 프로필은 브로커 없이 기동한다.
	}
}
