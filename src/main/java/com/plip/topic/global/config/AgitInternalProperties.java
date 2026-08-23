package com.plip.topic.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.agit")
public class AgitInternalProperties {

	/**
	 * agit 서비스 내부 base URL. Hash 미스 시 {@code GET /internal/v1/agits/{uuid}/members} 워밍에 사용한다.
	 */
	private String internalBaseUrl = "http://localhost:8083";
}
