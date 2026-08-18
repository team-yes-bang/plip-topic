package com.plip.topic.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoUploadedEvent(
		@JsonProperty("theme_uuid")
		@JsonAlias("themeUuid")
		UUID themeUuid,

		@JsonProperty("topic_uuid")
		@JsonAlias("topicUuid")
		UUID topicUuid,

		@JsonProperty("video_uuid")
		@JsonAlias("videoUuid")
		UUID videoUuid,

		@JsonProperty("user_uuid")
		@JsonAlias("userUuid")
		UUID userUuid
) {
}
