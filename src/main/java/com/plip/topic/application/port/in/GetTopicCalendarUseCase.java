package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicCalendarResult;

import java.time.YearMonth;
import java.util.UUID;

public interface GetTopicCalendarUseCase {

	TopicCalendarResult getCalendar(UUID agitUuid, YearMonth yearMonth);
}
