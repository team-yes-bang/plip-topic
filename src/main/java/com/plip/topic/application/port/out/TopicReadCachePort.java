package com.plip.topic.application.port.out;

import com.plip.topic.application.port.in.dto.TopicResult;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TopicReadCachePort {

	Optional<List<TopicResult>> getLatestTopics(UUID agitUuid);

	void putLatestTopics(UUID agitUuid, List<TopicResult> topics);

	Optional<List<LocalDate>> getCalendar(UUID agitUuid, YearMonth yearMonth);

	void putCalendar(UUID agitUuid, YearMonth yearMonth, List<LocalDate> activeDates);

	void evict(UUID agitUuid, LocalDate date);
}
