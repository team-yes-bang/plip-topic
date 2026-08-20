package com.plip.topic.application.service;

import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.DeleteTopicUseCase;
import com.plip.topic.application.port.in.GetTopicCalendarUseCase;
import com.plip.topic.application.port.in.GetTopicUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicCalendarResult;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService implements ListTopicsUseCase, GetTopicUseCase, GetTopicCalendarUseCase, CreateTopicUseCase, UpdateTopicUseCase, DeleteTopicUseCase {

	private final TopicPersistencePort topicPersistencePort;
	private final TopicReadCachePort topicReadCachePort;
	private final TopicViewerSnapshotPort topicViewerSnapshotPort;
	private final TopicCreatedEventPort topicCreatedEventPort;
	private final TopicAgitSyncEventPort topicAgitSyncEventPort;

	@Override
	@Transactional
	public TopicResult create(CreateTopicRequestDto request) {
		Topic saved = topicPersistencePort.save(Topic.create(
				request.getAgitUuid(),
				request.getCreatorUuid(),
				request.getTitle(),
				request.getStartAt()
		));
		topicReadCachePort.evict(saved.getAgitUuid(), saved.getStartAt().toLocalDate());
		saveSnapshotAfterCommit(saved);
		publishCreatedAfterCommit(saved);
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
		LocalDate oldDay = topic.getStartAt().toLocalDate();
		Topic updated = topic.update(request.getTitle(), request.getStartAt());
		Topic saved = topicPersistencePort.update(updated);
		LocalDate newDay = saved.getStartAt().toLocalDate();
		topicReadCachePort.evict(saved.getAgitUuid(), oldDay);
		if (!oldDay.equals(newDay)) {
			topicReadCachePort.evict(saved.getAgitUuid(), newDay);
		}
		saveSnapshotAfterCommit(saved);
		publishBoundAndStartedAfterCommit(saved);
		return TopicResult.from(saved);
	}

	@Override
	public TopicResult get(UUID topicUuid) {
		return TopicResult.from(loadForRead(topicUuid));
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
		topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		deleteSnapshotAfterCommit(topicUuid);
		publishUnboundAfterCommit(topic);
	}

	@Override
	public List<TopicResult> listByAgitUuidAndDate(UUID agitUuid, LocalDate date) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (date == null) {
			throw new IllegalArgumentException("date는 필수입니다.");
		}
		return topicReadCachePort.getDayTopics(agitUuid, date)
				.orElseGet(() -> {
					List<TopicResult> results = topicPersistencePort.findAllByAgitUuidAndDate(agitUuid, date).stream()
							.map(TopicResult::from)
							.toList();
					topicReadCachePort.putDayTopics(agitUuid, date, results);
					return results;
				});
	}

	@Override
	public TopicCalendarResult getCalendar(UUID agitUuid, YearMonth yearMonth) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (yearMonth == null) {
			throw new IllegalArgumentException("yearMonth는 필수입니다.");
		}
		List<LocalDate> activeDates = topicReadCachePort.getCalendar(agitUuid, yearMonth)
				.orElseGet(() -> {
					List<LocalDate> dates = topicPersistencePort.findActiveDates(agitUuid, yearMonth);
					topicReadCachePort.putCalendar(agitUuid, yearMonth, dates);
					return dates;
				});
		return TopicCalendarResult.builder()
				.agitUuid(agitUuid)
				.yearMonth(yearMonth)
				.activeDates(activeDates)
				.build();
	}

	private Topic loadForRead(UUID topicUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		return topicViewerSnapshotPort.findByTopicUuid(topicUuid)
				.orElseGet(() -> {
					Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
							.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
					saveSnapshotQuietly(topic);
					return topic;
				});
	}

	private void saveSnapshotAfterCommit(Topic topic) {
		afterCommit(() -> saveSnapshotQuietly(topic));
	}

	private void deleteSnapshotAfterCommit(UUID topicUuid) {
		afterCommit(() -> deleteSnapshotQuietly(topicUuid));
	}

	private void saveSnapshotQuietly(Topic topic) {
		try {
			topicViewerSnapshotPort.save(topic);
		} catch (RuntimeException exception) {
			log.warn("topic viewer snapshot save failed topicUuid={}", topic.getTopicUuid(), exception);
		}
	}

	private void deleteSnapshotQuietly(UUID topicUuid) {
		try {
			topicViewerSnapshotPort.delete(topicUuid);
		} catch (RuntimeException exception) {
			log.warn("topic viewer snapshot delete failed topicUuid={}", topicUuid, exception);
		}
	}

	private void publishCreatedAfterCommit(Topic saved) {
		afterCommit(() -> {
			topicCreatedEventPort.publishCreated(saved);
			topicAgitSyncEventPort.publishBoundAndStarted(saved);
		});
	}

	private void publishBoundAndStartedAfterCommit(Topic saved) {
		afterCommit(() -> topicAgitSyncEventPort.publishBoundAndStarted(saved));
	}

	private void publishUnboundAfterCommit(Topic topic) {
		afterCommit(() -> topicAgitSyncEventPort.publishUnbound(topic));
	}

	private void afterCommit(Runnable publish) {
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
