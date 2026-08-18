package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicResult;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ListTopicsUseCase {

	List<TopicResult> listByAgitUuidAndDate(UUID agitUuid, LocalDate date);
}
