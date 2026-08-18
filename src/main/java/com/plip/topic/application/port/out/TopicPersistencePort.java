package com.plip.topic.application.port.out;

import com.plip.topic.domain.model.Topic;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicPersistencePort {

	Topic save(Topic topic);

	Optional<Topic> findByTopicUuid(UUID topicUuid);

	List<Topic> findAllByAgitUuidAndDate(UUID agitUuid, LocalDate date);

	List<LocalDate> findActiveDates(UUID agitUuid, YearMonth yearMonth);

	Topic update(Topic topic);

	void deleteByTopicUuid(UUID topicUuid);
}
