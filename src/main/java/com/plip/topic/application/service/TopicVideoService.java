package com.plip.topic.application.service;

import com.plip.topic.application.port.in.AttachTopicVideoUseCase;
import com.plip.topic.application.port.in.DetachTopicVideoUseCase;
import com.plip.topic.application.port.in.ListTopicVideosUseCase;
import com.plip.topic.application.port.in.dto.TopicVideoResult;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicVideoEventPort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideoLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicVideoService implements AttachTopicVideoUseCase, DetachTopicVideoUseCase, ListTopicVideosUseCase {

	private final TopicPersistencePort topicPersistencePort;
	private final TopicReadCachePort topicReadCachePort;
	private final TopicViewerSnapshotPort topicViewerSnapshotPort;
	private final TopicVideoEventPort topicVideoEventPort;

	@Override
	@Transactional
	public boolean tryAttach(UUID topicUuid, UUID videoUuid, UUID userUuid) {
		if (topicUuid == null || videoUuid == null || userUuid == null) {
			return false;
		}
		try {
			boolean attached = topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid, userUuid);
			if (attached) {
				topicPersistencePort.findByTopicUuid(topicUuid).ifPresent(topic -> {
					topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
					saveSnapshotAfterCommit(topic);
				});
			}
			return attached;
		} catch (TopicVideoLimitException exception) {
			return false;
		}
	}

	@Override
	@Transactional
	public boolean attachOrThrow(UUID topicUuid, UUID videoUuid, UUID userUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		if (videoUuid == null) {
			throw new IllegalArgumentException("videoUuid는 필수입니다.");
		}
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid는 필수입니다.");
		}
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		boolean attached = topicPersistencePort.addVideoIfAbsent(topicUuid, videoUuid, userUuid);
		if (attached) {
			topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
			Topic projected = topicPersistencePort.findByTopicUuid(topicUuid).orElse(topic);
			saveSnapshotAfterCommit(projected);
			publishAttachedAfterCommit(topic, videoUuid, userUuid);
		}
		return attached;
	}

	@Override
	@Transactional
	public void detach(UUID topicUuid, UUID videoUuid, UUID userUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		topicPersistencePort.removeVideo(topicUuid, videoUuid, userUuid);
		topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		Topic projected = topicPersistencePort.findByTopicUuid(topicUuid).orElse(topic);
		saveSnapshotAfterCommit(projected);
	}

	@Override
	public List<TopicVideoResult> list(UUID topicUuid) {
		return loadForRead(topicUuid).getVideos().stream()
				.map(TopicVideoResult::from)
				.toList();
	}

	private Topic loadForRead(UUID topicUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		return topicViewerSnapshotPort.findByTopicUuid(topicUuid)
				.orElseGet(() -> {
					Topic loaded = topicPersistencePort.findByTopicUuid(topicUuid)
							.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
					saveSnapshotQuietly(loaded);
					return loaded;
				});
	}

	private void saveSnapshotAfterCommit(Topic topic) {
		afterCommit(() -> saveSnapshotQuietly(topic));
	}

	private void saveSnapshotQuietly(Topic topic) {
		try {
			topicViewerSnapshotPort.save(topic);
		} catch (RuntimeException exception) {
			log.warn("topic viewer snapshot save failed topicUuid={}", topic.getTopicUuid(), exception);
		}
	}

	private void afterCommit(Runnable action) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					action.run();
				}
			});
			return;
		}
		action.run();
	}

	private void publishAttachedAfterCommit(Topic topic, UUID videoUuid, UUID userUuid) {
		afterCommit(() -> topicVideoEventPort.publishAttached(
				topic.getTopicUuid(),
				topic.getAgitUuid(),
				videoUuid,
				userUuid
		));
	}
}
