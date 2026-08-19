package com.plip.topic.adapter.in.kafka;

import com.plip.topic.adapter.in.kafka.dto.VideoUploadedEvent;
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
public class VideoUploadedConsumer {

	private final AttachTopicVideoUseCase attachTopicVideoUseCase;

	@KafkaListener(
			topics = "${app.kafka.topics.video-uploaded:video.uploaded}",
			groupId = "${spring.kafka.consumer.group-id:topic-service}",
			containerFactory = "videoUploadedKafkaListenerContainerFactory"
	)
	public void consume(VideoUploadedEvent event) {
		if (event == null || event.topicUuid() == null || event.videoUuid() == null || event.userUuid() == null) {
			log.warn(
					"video.uploaded 토픽 연결 필드 누락 — skip topicUuid={} videoUuid={} userUuid={}",
					event == null ? null : event.topicUuid(),
					event == null ? null : event.videoUuid(),
					event == null ? null : event.userUuid()
			);
			return;
		}
		boolean attached = attachTopicVideoUseCase.tryAttach(event.topicUuid(), event.videoUuid(), event.userUuid());
		if (!attached) {
			log.info("video.uploaded 토픽 영상 미반영 — skip topicUuid={} videoUuid={}", event.topicUuid(), event.videoUuid());
		}
	}
}
