package com.plip.topic.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InternalProperties.class)
public class InternalApiConfig {
}
