package com.plip.topic.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.agit")
public class AgitGatewayProperties {

	/**
	 * API Gateway base URL. Topic looks up agit membership via {@code /api/agit/api/v1/agits/{uuid}}.
	 */
	private String gatewayBaseUrl = "http://localhost:8000";
}
