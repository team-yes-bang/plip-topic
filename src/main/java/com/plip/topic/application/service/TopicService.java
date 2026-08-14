package com.plip.topic.application.service;

import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.out.TopicPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService implements ListTopicsUseCase {

	private final TopicPersistencePort topicPersistencePort;

	@Override
	public List<TopicResult> listByAgitUuid(UUID agitUuid) {
		if (agitUuid == null) {
			throw new IllegalArgumentException("agitUuid는 필수입니다.");
		}
		return topicPersistencePort.findAllByAgitUuid(agitUuid).stream()
				.map(TopicResult::from)
				.toList();
	}
}
