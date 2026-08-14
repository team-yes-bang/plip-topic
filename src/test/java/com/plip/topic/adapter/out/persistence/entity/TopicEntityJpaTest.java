package com.plip.topic.adapter.out.persistence.entity;

import com.plip.topic.adapter.out.persistence.repository.TopicJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TopicEntityJpaTest {

	@Autowired
	private TopicJpaRepository topicJpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void save_assignsIdAndAuditingTimestamps() {
		TopicEntity saved = topicJpaRepository.saveAndFlush(newTopic("제목", UUID.randomUUID()));
		entityManager.clear();

		TopicEntity found = topicJpaRepository.findById(saved.getId()).orElseThrow();

		assertThat(found.getId()).isNotNull();
		assertThat(found.getTopicUuid()).isEqualTo(saved.getTopicUuid());
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getUpdatedAt()).isNotNull();
		assertThat(found.getDeletedAt()).isNull();
	}

	@Test
	void save_cascadesTopicVideos() {
		UUID videoUuid = UUID.randomUUID();
		TopicEntity topic = newTopic("영상 토픽", UUID.randomUUID());
		topic.addVideo(videoUuid);

		topicJpaRepository.saveAndFlush(topic);
		entityManager.clear();

		TopicEntity found = topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topic.getTopicUuid())
				.orElseThrow();

		assertThat(found.getVideos()).hasSize(1);
		assertThat(found.getVideos().get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(found.getVideos().get(0).getCreatedAt()).isNotNull();
		assertThat(found.getVideos().get(0).getDeletedAt()).isNull();
	}

	@Test
	void save_rejectsDuplicateVideoUuidOnSameTopic() {
		UUID videoUuid = UUID.randomUUID();
		TopicEntity topic = newTopic("중복 비디오", UUID.randomUUID());
		topic.addVideo(videoUuid);
		topic.addVideo(videoUuid);

		assertThatThrownBy(() -> topicJpaRepository.saveAndFlush(topic))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void findByTopicUuid_excludesSoftDeletedTopic() {
		TopicEntity topic = topicJpaRepository.saveAndFlush(newTopic("삭제될 토픽", UUID.randomUUID()));
		topic.addVideo(UUID.randomUUID());
		topicJpaRepository.saveAndFlush(topic);

		topic.softDelete(LocalDateTime.now());
		topicJpaRepository.saveAndFlush(topic);
		entityManager.clear();

		assertThat(topicJpaRepository.findByTopicUuidAndDeletedAtIsNull(topic.getTopicUuid())).isEmpty();
		assertThat(topicJpaRepository.findById(topic.getId())).isPresent();
	}

	@Test
	void findAllByAgitUuid_returnsOnlyActiveTopicsOfThatAgit() {
		UUID agitA = UUID.randomUUID();
		UUID agitB = UUID.randomUUID();
		topicJpaRepository.saveAndFlush(newTopic("A-1", agitA));
		topicJpaRepository.saveAndFlush(newTopic("A-2", agitA));
		topicJpaRepository.saveAndFlush(newTopic("B-1", agitB));
		entityManager.clear();

		assertThat(topicJpaRepository.findAllByAgitUuidAndDeletedAtIsNullOrderByStartAtDesc(agitA))
				.extracting(TopicEntity::getTitle)
				.containsExactlyInAnyOrder("A-1", "A-2");
	}

	private TopicEntity newTopic(String title, UUID agitUuid) {
		return TopicEntity.builder()
				.topicUuid(UUID.randomUUID())
				.agitUuid(agitUuid)
				.creatorUuid(UUID.randomUUID())
				.title(title)
				.startAt(LocalDateTime.of(2026, 8, 14, 0, 0))
				.layout("grid")
				.build();
	}
}
