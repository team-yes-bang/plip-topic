package com.plip.topic.application.service;

import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.DeleteTopicUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicVideoEventPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService implements ListTopicsUseCase, CreateTopicUseCase, UpdateTopicUseCase, DeleteTopicUseCase {

	private final TopicPersistencePort topicPersistencePort;
	private final TopicVideoEventPort topicVideoEventPort;

	@Override
	@Transactional
	public TopicResult create(CreateTopicRequestDto request) {
		Topic saved = topicPersistencePort.save(Topic.create(
				request.getAgitUuid(),
				request.getCreatorUuid(),
				request.getTitle(),
				request.getStartAt(),
				request.getVideoUuids()
		));
		publishAttachedAfterCommit(saved);
		return TopicResult.from(saved);
	}

	@Override
	@Transactional
	public TopicResult update(UUID topicUuid, UpdateTopicRequestDto request) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		Topic updated = topic.update(request.getTitle(), request.getStartAt());
		return TopicResult.from(topicPersistencePort.update(updated));
	}

	@Override
	@Transactional
	public void delete(UUID topicUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		topic.assertDeletable();
		topicPersistencePort.deleteByTopicUuid(topicUuid);
	}

	@Override
	public List<TopicResult> listByAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		return topicPersistencePort.findAllByAgitUuid(agitUuid).stream()
				.map(TopicResult::from)
				.toList();
	}

	private void publishAttachedAfterCommit(Topic saved) {
		List<UUID> videoUuids = saved.getVideos().stream()
				.map(TopicVideo::getVideoUuid)
				.toList();
		if (videoUuids.isEmpty()) {
			return;
		}
		Runnable publish = () -> videoUuids.forEach(videoUuid -> topicVideoEventPort.publishAttached(
				saved.getTopicUuid(),
				saved.getAgitUuid(),
				videoUuid,
				saved.getCreatorUuid()
		));
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					publish.run();
				}
			});
			return;
		}
		publish.run();
	}
}
