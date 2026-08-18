package com.plip.topic.adapter.in.kafka;

import com.plip.topic.adapter.in.kafka.dto.DiaryVideoBoundEvent;
import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DiaryVideoBoundConsumer {

	private final AttachTopicVideoUseCase attachTopicVideoUseCase;

	@KafkaListener(
			topics = "${app.kafka.topics.diary-video-bound:diary.video.bound}",
			groupId = "${spring.kafka.consumer.group-id:topic-service}",
			containerFactory = "diaryVideoBoundKafkaListenerContainerFactory"
	)
	public void consume(DiaryVideoBoundEvent event) {
		if (event == null || event.topicUuid() == null || event.videoUuid() == null) {
			log.warn(
					"diary.video.bound 토픽 연결 필드 누락 — skip topicUuid={} videoUuid={}",
					event == null ? null : event.topicUuid(),
					event == null ? null : event.videoUuid()
			);
			return;
		}
		boolean attached = attachTopicVideoUseCase.attach(event.topicUuid(), event.videoUuid());
		if (!attached) {
			log.info("diary.video.bound 토픽 영상 미반영 — skip topicUuid={} videoUuid={}", event.topicUuid(), event.videoUuid());
		}
	}
}
