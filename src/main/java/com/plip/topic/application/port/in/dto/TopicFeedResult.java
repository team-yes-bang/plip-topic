package com.plip.topic.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TopicFeedResult {

	private TopicResult current;
	private List<TopicResult> before;
	private List<TopicResult> after;

	public static TopicFeedResult empty() {
		return TopicFeedResult.builder()
				.current(null)
				.before(List.of())
				.after(List.of())
				.build();
	}
}
