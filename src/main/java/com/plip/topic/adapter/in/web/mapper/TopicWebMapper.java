package com.plip.topic.adapter.in.web.mapper;

import com.plip.topic.adapter.in.web.dto.CreateTopicRequest;
import com.plip.topic.adapter.in.web.dto.TopicResponseDto;
import com.plip.topic.adapter.in.web.dto.UpdateTopicRequest;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.TopicResult;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TopicWebMapper {

	public CreateTopicRequestDto toDto(CreateTopicRequest request) {
		return CreateTopicRequestDto.builder()
				.agitUuid(request.getAgitUuid())
				.creatorUuid(request.getCreatorUuid())
				.title(request.getTitle())
				.startAt(request.getStartAt())
				.layout(request.getLayout())
				.videoUuids(request.getVideoUuids())
				.build();
	}

	public UpdateTopicRequestDto toDto(UpdateTopicRequest request) {
		return UpdateTopicRequestDto.builder()
				.title(request.getTitle())
				.startAt(request.getStartAt())
				.layout(request.getLayout())
				.build();
	}

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
