package com.plip.topic.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plip.topic.adapter.out.kafka.dto.TopicAgitSyncEvent;
import com.plip.topic.adapter.out.kafka.dto.TopicCreatedEvent;
import com.plip.topic.adapter.out.kafka.dto.TopicVideoAttachedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers:localhost:9092}")
	private String bootstrapServers;

	@Bean
	public ProducerFactory<String, TopicAgitSyncEvent> topicAgitSyncProducerFactory() {
		return producerFactory();
	}

	@Bean
	public KafkaTemplate<String, TopicAgitSyncEvent> topicAgitSyncKafkaTemplate(
			ProducerFactory<String, TopicAgitSyncEvent> topicAgitSyncProducerFactory
	) {
		return new KafkaTemplate<>(topicAgitSyncProducerFactory);
	}

	@Bean
	public ProducerFactory<String, TopicCreatedEvent> topicCreatedProducerFactory() {
		return producerFactory();
	}

	@Bean
	public KafkaTemplate<String, TopicCreatedEvent> topicCreatedKafkaTemplate(
			ProducerFactory<String, TopicCreatedEvent> topicCreatedProducerFactory
	) {
		return new KafkaTemplate<>(topicCreatedProducerFactory);
	}

	@Bean
	public ProducerFactory<String, TopicVideoAttachedEvent> topicVideoAttachedProducerFactory() {
		return producerFactory();
	}

	@Bean
	public KafkaTemplate<String, TopicVideoAttachedEvent> topicVideoAttachedKafkaTemplate(
			ProducerFactory<String, TopicVideoAttachedEvent> topicVideoAttachedProducerFactory
	) {
		return new KafkaTemplate<>(topicVideoAttachedProducerFactory);
	}

	static ObjectMapper isoObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	private <T> ProducerFactory<String, T> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		JsonSerializer<T> valueSerializer = new JsonSerializer<>(isoObjectMapper());
		valueSerializer.setAddTypeInfo(false);
		return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), valueSerializer);
	}
}
