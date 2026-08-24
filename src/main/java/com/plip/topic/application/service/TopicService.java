package com.plip.topic.application.service;

import com.plip.topic.application.exception.ForbiddenActorException;
import com.plip.topic.application.exception.UnauthenticatedActorException;
import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.DeleteTopicUseCase;
import com.plip.topic.application.port.in.GetTopicCalendarUseCase;
import com.plip.topic.application.port.in.GetTopicFeedUseCase;
import com.plip.topic.application.port.in.GetTopicUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicCalendarResult;
import com.plip.topic.application.port.in.dto.TopicFeedResult;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicListStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService implements ListTopicsUseCase, GetTopicUseCase, GetTopicFeedUseCase, GetTopicCalendarUseCase, CreateTopicUseCase, UpdateTopicUseCase, DeleteTopicUseCase {

	private static final int LATEST_LIMIT = 10;
	private static final int DEFAULT_LIST_LIMIT = 10;
	private static final int MAX_LIST_LIMIT = 20;
	private static final int DEFAULT_FEED_NEIGHBORS = 1;
	private static final int MAX_FEED_NEIGHBORS = 3;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final TopicPersistencePort topicPersistencePort;
	private final TopicReadCachePort topicReadCachePort;
	private final TopicViewerSnapshotPort topicViewerSnapshotPort;
	private final TopicCreatedEventPort topicCreatedEventPort;
	private final TopicAgitSyncEventPort topicAgitSyncEventPort;
	private final AgitMembershipPort agitMembershipPort;

	@Override
	@Transactional
	public TopicResult create(CreateTopicRequestDto request, UUID actorUuid) {
		requireActor(actorUuid);
		if (request.getAgitUuid() == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		requireActiveMember(request.getAgitUuid(), actorUuid);
		Topic saved = topicPersistencePort.save(Topic.create(
				request.getAgitUuid(),
				actorUuid,
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
	public TopicResult update(UUID topicUuid, UpdateTopicRequestDto request, UUID actorUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		requireActor(actorUuid);
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		requireCreatorOrHost(topic, actorUuid);
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
	public TopicResult get(UUID topicUuid, UUID actorUuid) {
		Topic topic = loadForRead(topicUuid);
		requireActiveMember(topic.getAgitUuid(), actorUuid);
		return TopicResult.from(topic);
	}

	@Override
	@Transactional
	public void delete(UUID topicUuid, UUID actorUuid) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		requireActor(actorUuid);
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		requireCreatorOrHost(topic, actorUuid);
		topic.assertDeletable();
		topicPersistencePort.deleteByTopicUuid(topicUuid);
		topicReadCachePort.evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		deleteSnapshotAfterCommit(topicUuid);
		publishUnboundAfterCommit(topic);
	}

	@Override
	public List<TopicResult> listLatestByAgitUuid(UUID agitUuid, UUID actorUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		requireActiveMember(agitUuid, actorUuid);
		return topicReadCachePort.getLatestTopics(agitUuid)
				.orElseGet(() -> {
					List<TopicResult> results = topicPersistencePort.findLatestByAgitUuid(agitUuid, LATEST_LIMIT).stream()
							.map(TopicResult::from)
							.toList();
					topicReadCachePort.putLatestTopics(agitUuid, results);
					return results;
				});
	}

	@Override
	public List<TopicResult> listByAgitUuidAndStatus(UUID agitUuid, TopicListStatus status, Integer limit, UUID actorUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (status == null) {
			throw new IllegalArgumentException("status는 필수입니다.");
		}
		requireActiveMember(agitUuid, actorUuid);
		LocalDate today = LocalDate.now(KST);
		return topicPersistencePort.findByAgitUuidAndListStatus(agitUuid, status, today, resolveListLimit(limit)).stream()
				.map(TopicResult::from)
				.toList();
	}

	@Override
	public TopicFeedResult feed(UUID agitUuid, UUID topicUuid, LocalDate date, Integer before, Integer after, UUID actorUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if ((topicUuid == null) == (date == null)) {
			throw new IllegalArgumentException("topicUuid 또는 date 중 하나만 필요합니다.");
		}
		requireActiveMember(agitUuid, actorUuid);
		LocalDate today = LocalDate.now(KST);
		int beforeCount = resolveFeedNeighbors(before);
		int afterCount = resolveFeedNeighbors(after);
		Topic current = topicUuid != null
				? resolveFeedCurrentByTopic(agitUuid, topicUuid, today)
				: topicPersistencePort.findFeedAnchorOnDate(agitUuid, today, date).orElse(null);
		if (current == null) {
			return TopicFeedResult.empty();
		}
		return neighborsOf(agitUuid, today, current, beforeCount, afterCount);
	}

	private Topic resolveFeedCurrentByTopic(UUID agitUuid, UUID topicUuid, LocalDate today) {
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		if (!agitUuid.equals(topic.getAgitUuid())) {
			throw new IllegalArgumentException("agitUuid가 토픽과 일치하지 않습니다.");
		}
		if (!isInFeedWindow(topic, today)) {
			return null;
		}
		if (isPastStart(topic, today) && topic.videoCount() == 0) {
			return null;
		}
		return topic;
	}

	private boolean isInFeedWindow(Topic topic, LocalDate today) {
		LocalDateTime nextDayStart = today.plusDays(1).atStartOfDay();
		return topic.getStartAt() != null && topic.getStartAt().isBefore(nextDayStart);
	}

	private boolean isPastStart(Topic topic, LocalDate today) {
		return topic.getStartAt() != null && topic.getStartAt().isBefore(today.atStartOfDay());
	}

	private TopicFeedResult neighborsOf(
			UUID agitUuid,
			LocalDate today,
			Topic current,
			int beforeCount,
			int afterCount
	) {
		List<Topic> ongoing = topicPersistencePort.findFeedOngoingWithVideos(agitUuid, today);
		int ongoingIndex = indexOf(ongoing, current.getTopicUuid());
		List<Topic> before = new ArrayList<>();
		List<Topic> after = new ArrayList<>();
		if (ongoingIndex >= 0) {
			for (int i = ongoingIndex - 1; i >= 0 && before.size() < beforeCount; i--) {
				before.add(ongoing.get(i));
			}
			for (int i = ongoingIndex + 1; i < ongoing.size() && after.size() < afterCount; i++) {
				after.add(ongoing.get(i));
			}
			if (after.size() < afterCount) {
				after.addAll(topicPersistencePort.findFeedPastFromStart(agitUuid, today, afterCount - after.size()));
			}
		} else {
			before.addAll(topicPersistencePort.findFeedPastNewerThan(agitUuid, today, current, beforeCount));
			if (before.size() < beforeCount) {
				for (int i = ongoing.size() - 1; i >= 0 && before.size() < beforeCount; i--) {
					before.add(ongoing.get(i));
				}
			}
			after.addAll(topicPersistencePort.findFeedPastOlderThan(agitUuid, today, current, afterCount));
		}
		return TopicFeedResult.builder()
				.current(TopicResult.from(current))
				.before(before.stream().map(TopicResult::from).toList())
				.after(after.stream().map(TopicResult::from).toList())
				.build();
	}

	private static int indexOf(List<Topic> topics, UUID topicUuid) {
		for (int i = 0; i < topics.size(); i++) {
			if (topics.get(i).getTopicUuid().equals(topicUuid)) {
				return i;
			}
		}
		return -1;
	}

	private void requireActor(UUID actorUuid) {
		if (actorUuid == null) {
			throw new UnauthenticatedActorException();
		}
	}

	private AgitMembership requireActiveMember(UUID agitUuid, UUID actorUuid) {
		requireActor(actorUuid);
		return agitMembershipPort.findActiveMember(agitUuid, actorUuid)
				.orElseThrow(ForbiddenActorException::new);
	}

	private void requireCreatorOrHost(Topic topic, UUID actorUuid) {
		AgitMembership membership = requireActiveMember(topic.getAgitUuid(), actorUuid);
		if (actorUuid.equals(topic.getCreatorUuid()) || membership.isHost()) {
			return;
		}
		throw new ForbiddenActorException();
	}

	private int resolveListLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_LIST_LIMIT;
		}
		return Math.min(MAX_LIST_LIMIT, Math.max(1, limit));
	}

	private int resolveFeedNeighbors(Integer count) {
		if (count == null) {
			return DEFAULT_FEED_NEIGHBORS;
		}
		return Math.min(MAX_FEED_NEIGHBORS, Math.max(0, count));
	}

	@Override
	public TopicCalendarResult getCalendar(UUID agitUuid, YearMonth yearMonth, UUID actorUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		if (yearMonth == null) {
			throw new IllegalArgumentException("yearMonth는 필수입니다.");
		}
		requireActiveMember(agitUuid, actorUuid);
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
