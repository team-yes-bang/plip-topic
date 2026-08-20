package com.plip.topic.adapter.out.snapshot.document;

import com.plip.topic.domain.model.TopicVideo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TopicViewerVideoDocument {

	private String videoUuid;
	private String userUuid;
	private LocalDateTime createdAt;

	public static TopicViewerVideoDocument from(TopicVideo video) {
		TopicViewerVideoDocument document = new TopicViewerVideoDocument();
		document.setVideoUuid(video.getVideoUuid().toString());
		document.setUserUuid(video.getUserUuid().toString());
		document.setCreatedAt(video.getCreatedAt());
		return document;
	}

	public TopicVideo toDomain() {
		return TopicVideo.reconstitute(
				UUID.fromString(videoUuid),
				UUID.fromString(userUuid),
				createdAt,
				null
		);
	}
}
