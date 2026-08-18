package com.plip.topic.application.service;

import com.plip.topic.application.port.in.CreateTopicUseCase;
import com.plip.topic.application.port.in.ListTopicsUseCase;
import com.plip.topic.application.port.in.UpdateTopicUseCase;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TopicService implements ListTopicsUseCase, CreateTopicUseCase, UpdateTopicUseCase {

	private final TopicPersistencePort topicPersistencePort;

	@Override
	@Transactional
	public TopicResult create(CreateTopicRequestDto request) {
		Topic saved = topicPersistencePort.save(Topic.create(
				request.getAgitUuid(),
				request.getCreatorUuid(),
				request.getTitle(),
				request.getStartAt(),
				request.getLayout(),
				request.getVideoUuids()
		));
		return TopicResult.from(saved);
	}

	@Override
	@Transactional
	public TopicResult update(UUID topicUuid, UpdateTopicRequestDto request) {
		if (topicUuid == null) {
			throw new IllegalArgumentException("topicUuid는 필수입니다.");
		}
		Topic topic = topicPersistencePort.findByTopicUuid(topicUuid)
				.orElseThrow(() -> new IllegalArgumentException("토픽이 존재하지 않습니다."));
		Topic updated = topic.update(request.getTitle(), request.getStartAt(), request.getLayout());
		return TopicResult.from(topicPersistencePort.update(updated));
	}

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
