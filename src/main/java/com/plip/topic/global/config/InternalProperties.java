package com.plip.topic.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.internal")
public record InternalProperties(String apiKey) {
}
