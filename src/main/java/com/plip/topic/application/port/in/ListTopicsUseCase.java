package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.TopicResult;

import java.util.List;
import java.util.UUID;

public interface ListTopicsUseCase {

	List<TopicResult> listLatestByAgitUuid(UUID agitUuid);
}
