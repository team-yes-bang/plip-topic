package com.plip.topic.application.port.in;

import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicResult;

public interface CreateTopicUseCase {

	TopicResult create(CreateTopicRequestDto request);
}
