package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicListStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicPersistencePort {

	Topic save(Topic topic);

	Optional<Topic> findByTopicUuid(UUID topicUuid);

	List<Topic> findLatestByAgitUuid(UUID agitUuid, int limit);

	List<Topic> findByAgitUuidAndListStatus(UUID agitUuid, TopicListStatus status, LocalDate today, int limit);

	List<Topic> findFeedOngoingWithVideos(UUID agitUuid, LocalDate today);

	List<Topic> findFeedPastFromStart(UUID agitUuid, LocalDate today, int limit);

	List<Topic> findFeedPastOlderThan(UUID agitUuid, LocalDate today, Topic current, int limit);

	List<Topic> findFeedPastNewerThan(UUID agitUuid, LocalDate today, Topic current, int limit);

	Optional<Topic> findFeedAnchorOnDate(UUID agitUuid, LocalDate today, LocalDate date);

	List<LocalDate> findActiveDates(UUID agitUuid, YearMonth yearMonth);

	Topic update(Topic topic);

	void deleteByTopicUuid(UUID topicUuid);

	boolean addVideoIfAbsent(UUID topicUuid, UUID videoUuid, UUID userUuid);

	void removeVideo(UUID topicUuid, UUID videoUuid, UUID userUuid);

	List<UUID> findAgitUuidsByVideoUuid(UUID videoUuid);
}
