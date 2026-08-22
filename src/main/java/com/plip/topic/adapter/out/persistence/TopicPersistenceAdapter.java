package com.plip.topic.adapter.out.persistence;

import com.plip.topic.adapter.out.persistence.entity.TopicCalendarDayEntity;
import com.plip.topic.adapter.out.persistence.entity.TopicEntity;
import com.plip.topic.adapter.out.persistence.mapper.TopicPersistenceMapper;
import com.plip.topic.adapter.out.persistence.repository.TopicCalendarDayJpaRepository;
import com.plip.topic.adapter.out.persistence.repository.TopicJpaRepository;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicListStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicPersistenceAdapter implements TopicPersistencePort {

	private final TopicJpaRepository topicJpaRepository;
	private final TopicCalendarDayJpaRepository topicCalendarDayJpaRepository;
	private final TopicPersistenceMapper topicPersistenceMapper;

	@Override
	@Transactional
	public Topic save(Topic topic) {
		TopicEntity saved = topicJpaRepository.save(topicPersistenceMapper.toEntity(topic));
		applyCalendarDelta(saved.getAgitUuid(), saved.getStartAt().toLocalDate(), 1, saved.getVideos().size());
		return topicPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Topic> findByTopicUuid(UUID topicUuid) {
		return topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.map(topicPersistenceMapper::toDomain);
	}

	@Override
	public List<Topic> findLatestByAgitUuid(UUID agitUuid, int limit) {
		return topicJpaRepository.findLatestByAgitUuid(agitUuid, PageRequest.of(0, limit))
				.stream()
				.map(topicPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<Topic> findByAgitUuidAndListStatus(UUID agitUuid, TopicListStatus status, LocalDate today, int limit) {
		LocalDateTime dayStart = today.atStartOfDay();
		LocalDateTime nextDayStart = today.plusDays(1).atStartOfDay();
		List<TopicEntity> entities = switch (status) {
			case ONGOING -> topicJpaRepository.findOngoingByAgitUuid(
					agitUuid, dayStart, nextDayStart, PageRequest.of(0, limit));
			case UPCOMING -> topicJpaRepository.findUpcomingByAgitUuid(
					agitUuid, nextDayStart, PageRequest.of(0, limit));
			case PAST -> topicJpaRepository.findPastByAgitUuid(
					agitUuid, dayStart, PageRequest.of(0, limit));
		};
		return entities.stream().map(topicPersistenceMapper::toDomain).toList();
	}

	@Override
	public List<LocalDate> findActiveDates(UUID agitUuid, YearMonth yearMonth) {
		LocalDate from = yearMonth.atDay(1);
		LocalDate to = yearMonth.plusMonths(1).atDay(1);
		return topicCalendarDayJpaRepository.findActiveDays(agitUuid, from, to);
	}

	@Override
	@Transactional
	public Topic update(Topic topic) {
		TopicEntity entity = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topic.getTopicUuid())
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		LocalDate oldDay = entity.getStartAt().toLocalDate();
		int videoCount = entity.getVideos().size();
		entity.update(topic.getTitle(), topic.getStartAt());
		LocalDate newDay = entity.getStartAt().toLocalDate();
		if (!oldDay.equals(newDay)) {
			applyCalendarDelta(entity.getAgitUuid(), oldDay, -1, -videoCount);
			applyCalendarDelta(entity.getAgitUuid(), newDay, 1, videoCount);
		}
		return topicPersistenceMapper.toDomain(entity);
	}

	@Override
	@Transactional
	public void deleteByTopicUuid(UUID topicUuid) {
		TopicEntity entity = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		int videoCount = entity.getVideos().size();
		applyCalendarDelta(entity.getAgitUuid(), entity.getStartAt().toLocalDate(), -1, -videoCount);
		entity.softDelete(LocalDateTime.now());
	}

	@Override
	@Transactional
	public boolean addVideoIfAbsent(UUID topicUuid, UUID videoUuid, UUID userUuid) {
		if (topicUuid == null || videoUuid == null || userUuid == null) {
			return false;
		}
		return topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.map(entity -> {
					Topic topic = topicPersistenceMapper.toDomain(entity);
					int before = topic.videoCount();
					Topic attached = topic.attachVideo(userUuid, videoUuid);
					if (attached.videoCount() == before) {
						return false;
					}
					entity.addVideo(videoUuid, userUuid);
					applyCalendarDelta(entity.getAgitUuid(), entity.getStartAt().toLocalDate(), 0, 1);
					return true;
				})
				.orElse(false);
	}

	@Override
	@Transactional
	public void removeVideo(UUID topicUuid, UUID videoUuid, UUID userUuid) {
		TopicEntity entity = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		topicPersistenceMapper.toDomain(entity).detachVideo(userUuid, videoUuid);
		entity.removeVideo(videoUuid);
		applyCalendarDelta(entity.getAgitUuid(), entity.getStartAt().toLocalDate(), 0, -1);
	}

	private void applyCalendarDelta(UUID agitUuid, LocalDate day, int topicDelta, int videoDelta) {
		TopicCalendarDayEntity row = topicCalendarDayJpaRepository.findByAgitUuidAndDay(agitUuid, day)
				.orElseGet(() -> TopicCalendarDayEntity.create(agitUuid, day));
		row.apply(topicDelta, videoDelta);
		topicCalendarDayJpaRepository.save(row);
	}
}
