package com.plip.topic.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TopicCalendarResult {

	private final UUID agitUuid;
	private final YearMonth yearMonth;
	private final List<LocalDate> activeDates;
}
