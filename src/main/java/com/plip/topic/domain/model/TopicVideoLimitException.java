package com.plip.topic.domain.model;

public class TopicVideoLimitException extends IllegalArgumentException {

	public TopicVideoLimitException() {
		super("이미 이 토픽에 영상을 올렸습니다.");
	}
}
