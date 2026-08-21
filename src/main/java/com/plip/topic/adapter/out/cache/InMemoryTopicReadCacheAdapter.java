package com.plip.topic.adapter.out.cache;

import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.out.TopicReadCachePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class InMemoryTopicReadCacheAdapter implements TopicReadCachePort {

	private final ConcurrentHashMap<String, List<TopicResult>> latestTopics = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, List<LocalDate>> calendars = new ConcurrentHashMap<>();

	@Override
	public Optional<List<TopicResult>> getLatestTopics(UUID agitUuid) {
		return Optional.ofNullable(latestTopics.get(latestKey(agitUuid)));
	}

	@Override
	public void putLatestTopics(UUID agitUuid, List<TopicResult> topics) {
		latestTopics.put(latestKey(agitUuid), List.copyOf(topics));
	}

	@Override
	public Optional<List<LocalDate>> getCalendar(UUID agitUuid, YearMonth yearMonth) {
		return Optional.ofNullable(calendars.get(calendarKey(agitUuid, yearMonth)));
	}

	@Override
	public void putCalendar(UUID agitUuid, YearMonth yearMonth, List<LocalDate> activeDates) {
		calendars.put(calendarKey(agitUuid, yearMonth), List.copyOf(activeDates));
	}

	@Override
	public void evict(UUID agitUuid, LocalDate date) {
		latestTopics.remove(latestKey(agitUuid));
		calendars.remove(calendarKey(agitUuid, YearMonth.from(date)));
	}

	private static String latestKey(UUID agitUuid) {
		return "topic:latest:" + agitUuid;
	}

	private static String calendarKey(UUID agitUuid, YearMonth yearMonth) {
		return "topic:calendar:" + agitUuid + ":" + yearMonth;
	}
}
