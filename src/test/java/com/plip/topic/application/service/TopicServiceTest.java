package com.plip.topic.application.service;

import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@Mock
	private TopicReadCachePort topicReadCachePort;

	@Mock
	private TopicCreatedEventPort topicCreatedEventPort;

	@Mock
	private TopicAgitSyncEventPort topicAgitSyncEventPort;

	@InjectMocks
	private TopicService topicService;

	@Test
	void listByAgitUuidAndDate_mapsPersistedTopics() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		LocalDate date = LocalDate.of(2026, 8, 14);
		Topic topic = Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"주말 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0)
		).attachVideo(userUuid, videoUuid);
		given(topicReadCachePort.getDayTopics(agitUuid, date)).willReturn(Optional.empty());
		given(topicPersistencePort.findAllByAgitUuidAndDate(agitUuid, date)).willReturn(List.of(topic));

		var results = topicService.listByAgitUuidAndDate(agitUuid, date);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTitle()).isEqualTo("주말 모임");
		assertThat(results.get(0).getAgitUuid()).isEqualTo(agitUuid);
		assertThat(results.get(0).getVideoCount()).isEqualTo(1);
		assertThat(results.get(0).uploadedBy(userUuid)).isTrue();
		verify(topicReadCachePort).putDayTopics(agitUuid, date, results);
	}

	@Test
	void listByAgitUuidAndDate_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.listByAgitUuidAndDate(null, LocalDate.of(2026, 8, 14)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void listByAgitUuidAndDate_requiresDate() {
		assertThatThrownBy(() -> topicService.listByAgitUuidAndDate(UUID.randomUUID(), null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("date는 필수입니다.");
	}

	@Test
	void get_returnsTopic() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		var result = topicService.get(topic.getTopicUuid());

		assertThat(result.getTitle()).isEqualTo("제목");
		assertThat(result.getVideoCount()).isZero();
	}

	@Test
	void getCalendar_returnsActiveDates() {
		UUID agitUuid = UUID.randomUUID();
		YearMonth yearMonth = YearMonth.of(2026, 8);
		List<LocalDate> dates = List.of(LocalDate.of(2026, 8, 14));
		given(topicReadCachePort.getCalendar(agitUuid, yearMonth)).willReturn(Optional.empty());
		given(topicPersistencePort.findActiveDates(agitUuid, yearMonth)).willReturn(dates);

		var result = topicService.getCalendar(agitUuid, yearMonth);

		assertThat(result.getActiveDates()).containsExactly(LocalDate.of(2026, 8, 14));
		verify(topicReadCachePort).putCalendar(agitUuid, yearMonth, dates);
	}

	@Test
	void create_persistsTopicWithoutVideos() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDateTime startAt = LocalDateTime.of(2026, 8, 18, 0, 0);
		given(topicPersistencePort.save(any(Topic.class))).willAnswer(invocation -> invocation.getArgument(0));

		var result = topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(agitUuid)
				.creatorUuid(creatorUuid)
				.title("점심 메뉴")
				.startAt(startAt)
				.build());

		assertThat(result.getTopicUuid()).isNotNull();
		assertThat(result.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(result.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(result.getTitle()).isEqualTo("점심 메뉴");
		assertThat(result.getStartAt()).isEqualTo(startAt);
		assertThat(result.getVideoCount()).isZero();
		verify(topicPersistencePort).save(any(Topic.class));
		verify(topicReadCachePort).evict(agitUuid, startAt.toLocalDate());
		verify(topicCreatedEventPort).publishCreated(any(Topic.class));
		verify(topicAgitSyncEventPort).publishBoundAndStarted(any(Topic.class));
	}

	@Test
	void create_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.creatorUuid(UUID.randomUUID())
				.title("제목")
				.build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void create_requiresCreatorUuid() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(UUID.randomUUID())
				.title("제목")
				.build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("creatorUuid는 필수입니다.");
	}

	@Test
	void update_changesProvidedFields() {
		Topic existing = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));
		given(topicPersistencePort.update(any(Topic.class))).willAnswer(invocation -> invocation.getArgument(0));

		var result = topicService.update(existing.getTopicUuid(), UpdateTopicRequestDto.builder()
				.title("저녁 메뉴")
				.build());

		assertThat(result.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(result.getStartAt()).isEqualTo(existing.getStartAt());
		assertThat(result.getTopicUuid()).isEqualTo(existing.getTopicUuid());
		verify(topicPersistencePort).update(any(Topic.class));
	}

	@Test
	void update_throwsWhenMissing() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicService.update(topicUuid, UpdateTopicRequestDto.builder()
				.title("제목")
				.build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void delete_softDeletesEmptyTopic() {
		Topic existing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));

		topicService.delete(existing.getTopicUuid());

		verify(topicPersistencePort).deleteByTopicUuid(existing.getTopicUuid());
		verify(topicAgitSyncEventPort).publishUnbound(existing);
	}

	@Test
	void delete_rejectsTopicWithVideos() {
		Topic existing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(UUID.randomUUID(), UUID.randomUUID());
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));

		assertThatThrownBy(() -> topicService.delete(existing.getTopicUuid()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("영상이 있는 토픽은 삭제할 수 없습니다.");
		verify(topicPersistencePort, never()).deleteByTopicUuid(existing.getTopicUuid());
		verify(topicAgitSyncEventPort, never()).publishUnbound(any(Topic.class));
	}

	@Test
	void delete_throwsWhenMissing() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicService.delete(topicUuid))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
		verify(topicAgitSyncEventPort, never()).publishUnbound(any(Topic.class));
	}
}
