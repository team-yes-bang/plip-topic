package com.plip.topic.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.video")
public class VideoInternalProperties {

	/**
	 * video 서비스 내부 base URL. attach 시 {@code GET /internal/videos/{uuid}} 소유권 조회에 사용한다.
	 */
	private String internalBaseUrl = "http://localhost:8085";

	private String internalApiKey = "";
}
