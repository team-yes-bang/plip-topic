package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.domain.model.TopicListStatus;

import java.util.List;
import java.util.UUID;

public interface ListTopicsUseCase {

	List<TopicResult> listLatestByAgitUuid(UUID agitUuid);

	List<TopicResult> listByAgitUuidAndStatus(UUID agitUuid, TopicListStatus status, Integer limit);
}
