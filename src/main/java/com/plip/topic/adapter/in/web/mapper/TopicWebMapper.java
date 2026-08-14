package com.plip.topic.adapter.in.web.mapper;

import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.application.port.in.dto.TopicResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TopicWebMapper {

	public TopicResponseDto toDto(TopicResult result) {
		return TopicResponseDto.builder()
				.topicUuid(result.getTopicUuid())
				.agitUuid(result.getAgitUuid())
				.creatorUuid(result.getCreatorUuid())
				.title(result.getTitle())
				.startAt(result.getStartAt())
				.layout(result.getLayout())
				.videoUuids(result.getVideoUuids())
				.createdAt(result.getCreatedAt())
				.build();
	}

	public List<TopicResponseDto> toDtoList(List<TopicResult> results) {
		return results.stream().map(this::toDto).toList();
	}
}
