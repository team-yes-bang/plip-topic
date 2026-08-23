package com.plip.topic.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@Profile("!test")
@EnableConfigurationProperties(AgitInternalProperties.class)
public class AgitInternalClientConfig {

	@Bean
	RestClient agitInternalRestClient(AgitInternalProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(3));
		return RestClient.builder()
				.baseUrl(properties.getInternalBaseUrl())
				.requestFactory(requestFactory)
				.build();
	}
}
