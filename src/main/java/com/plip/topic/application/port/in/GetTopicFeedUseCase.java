package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicFeedResult;

import java.time.LocalDate;
import java.util.UUID;

public interface GetTopicFeedUseCase {

	TopicFeedResult feed(UUID agitUuid, UUID topicUuid, LocalDate date, Integer before, Integer after);
}
