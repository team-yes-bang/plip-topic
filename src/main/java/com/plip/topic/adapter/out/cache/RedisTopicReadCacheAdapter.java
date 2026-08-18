package com.plip.topic.adapter.out.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.out.TopicReadCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisTopicReadCacheAdapter implements TopicReadCachePort {

	private static final Duration TTL = Duration.ofMinutes(10);
	private static final TypeReference<List<TopicResult>> DAY_TYPE = new TypeReference<>() {
	};
	private static final TypeReference<List<LocalDate>> CALENDAR_TYPE = new TypeReference<>() {
	};

	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public Optional<List<TopicResult>> getDayTopics(UUID agitUuid, LocalDate date) {
		return read(dayKey(agitUuid, date), DAY_TYPE);
	}

	@Override
	public void putDayTopics(UUID agitUuid, LocalDate date, List<TopicResult> topics) {
		write(dayKey(agitUuid, date), topics);
	}

	@Override
	public Optional<List<LocalDate>> getCalendar(UUID agitUuid, YearMonth yearMonth) {
		return read(calendarKey(agitUuid, yearMonth), CALENDAR_TYPE);
	}

	@Override
	public void putCalendar(UUID agitUuid, YearMonth yearMonth, List<LocalDate> activeDates) {
		write(calendarKey(agitUuid, yearMonth), activeDates);
	}

	@Override
	public void evict(UUID agitUuid, LocalDate date) {
		try {
			redisTemplate.delete(List.of(
					dayKey(agitUuid, date),
					calendarKey(agitUuid, YearMonth.from(date))
			));
		} catch (Exception exception) {
			log.warn("topic redis evict failed agitUuid={} date={}", agitUuid, date, exception);
		}
	}

	private <T> Optional<T> read(String key, TypeReference<T> type) {
		try {
			String json = redisTemplate.opsForValue().get(key);
			if (json == null) {
				return Optional.empty();
			}
			return Optional.of(objectMapper.readValue(json, type));
		} catch (Exception exception) {
			log.warn("topic redis get failed key={}", key, exception);
			return Optional.empty();
		}
	}

	private void write(String key, Object value) {
		try {
			redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), TTL);
		} catch (Exception exception) {
			log.warn("topic redis put failed key={}", key, exception);
		}
	}

	private static String dayKey(UUID agitUuid, LocalDate date) {
		return "topic:day:" + agitUuid + ":" + date;
	}

	private static String calendarKey(UUID agitUuid, YearMonth yearMonth) {
		return "topic:calendar:" + agitUuid + ":" + yearMonth;
	}
}
