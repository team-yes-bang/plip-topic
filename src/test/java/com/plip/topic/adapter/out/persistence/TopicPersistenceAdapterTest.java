package com.plip.topic.adapter.out.persistence;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TopicPersistenceAdapterTest {

	@Autowired
	private TopicPersistencePort topicPersistencePort;

	@Test
	void saveAndFind_roundTripsTopicWithVideos() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 14, 10, 0);

		Topic saved = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "주말 모임", startAt, List.of(videoUuid))
		);

		Topic found = topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow();

		assertThat(found.getTopicUuid()).isEqualTo(saved.getTopicUuid());
		assertThat(found.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(found.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(found.getTitle()).isEqualTo("주말 모임");
		assertThat(found.getStartAt()).isEqualTo(startAt);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getVideos()).extracting(video -> video.getVideoUuid()).containsExactly(videoUuid);
		assertThat(found.isDeleted()).isFalse();
	}

	@Test
	void update_changesTitleAndKeepsStartAt() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "점심 메뉴", LocalDateTime.of(2026, 8, 18, 0, 0), List.of())
		);

		Topic updated = saved.update("저녁 메뉴", null);
		Topic found = topicPersistencePort.update(updated);

		assertThat(found.getTopicUuid()).isEqualTo(saved.getTopicUuid());
		assertThat(found.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(found.getStartAt()).isEqualTo(saved.getStartAt());
	}

	@Test
	void update_throwsWhenMissing() {
		Topic missing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of());

		assertThatThrownBy(() -> topicPersistencePort.update(missing))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void deleteByTopicUuid_hidesTopicFromFind() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "삭제 대상", null, List.of(UUID.randomUUID()))
		);

		topicPersistencePort.deleteByTopicUuid(saved.getTopicUuid());

		assertThat(topicPersistencePort.findByTopicUuid(saved.getTopicUuid())).isEmpty();
	}

	@Test
	void deleteByTopicUuid_throwsWhenMissing() {
		assertThatThrownBy(() -> topicPersistencePort.deleteByTopicUuid(UUID.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void findAllByAgitUuid_returnsOnlyThatAgitOrderedByStartAtDesc() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		topicPersistencePort.save(Topic.create(
				agitUuid, creatorUuid, "old", LocalDateTime.of(2026, 8, 1, 0, 0), List.of()));
		topicPersistencePort.save(Topic.create(
				agitUuid, creatorUuid, "new", LocalDateTime.of(2026, 8, 14, 0, 0), List.of()));
		topicPersistencePort.save(Topic.create(
				UUID.randomUUID(), creatorUuid, "other", LocalDateTime.of(2026, 8, 20, 0, 0), List.of()));

		List<Topic> found = topicPersistencePort.findAllByAgitUuid(agitUuid);

		assertThat(found).extracting(Topic::getTitle).containsExactly("new", "old");
	}

	@Test
	void addVideoIfAbsent_appendsNewVideo() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of())
		);
		UUID videoUuid = UUID.randomUUID();

		boolean attached = topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), videoUuid);
		Topic found = topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow();

		assertThat(attached).isTrue();
		assertThat(found.getVideos()).extracting(video -> video.getVideoUuid()).containsExactly(videoUuid);
	}

	@Test
	void addVideoIfAbsent_isIdempotent() {
		UUID videoUuid = UUID.randomUUID();
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null, List.of(videoUuid))
		);

		boolean attached = topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), videoUuid);

		assertThat(attached).isFalse();
		assertThat(topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow().getVideos())
				.hasSize(1);
	}

	@Test
	void addVideoIfAbsent_returnsFalseWhenTopicMissing() {
		assertThat(topicPersistencePort.addVideoIfAbsent(UUID.randomUUID(), UUID.randomUUID())).isFalse();
	}
}
