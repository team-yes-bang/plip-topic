package com.plip.topic.application.service;

import com.plip.topic.application.exception.ForbiddenActorException;
import com.plip.topic.application.exception.UnauthenticatedActorException;
import com.plip.topic.application.port.in.dto.CreateTopicRequestDto;
import com.plip.topic.application.port.in.dto.UpdateTopicRequestDto;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicAgitSyncEventPort;
import com.plip.topic.application.port.out.TopicCreatedEventPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicListStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
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
	private TopicViewerSnapshotPort topicViewerSnapshotPort;

	@Mock
	private TopicCreatedEventPort topicCreatedEventPort;

	@Mock
	private TopicAgitSyncEventPort topicAgitSyncEventPort;

	@Mock
	private AgitMembershipPort agitMembershipPort;

	@InjectMocks
	private TopicService topicService;

	@Test
	void listLatestByAgitUuid_mapsPersistedTopics() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"주말 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0)
		).attachVideo(userUuid, videoUuid);
		given(topicReadCachePort.getLatestTopics(agitUuid)).willReturn(Optional.empty());
		given(topicPersistencePort.findLatestByAgitUuid(agitUuid, 10)).willReturn(List.of(topic));

		var results = topicService.listLatestByAgitUuid(agitUuid);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTitle()).isEqualTo("주말 모임");
		assertThat(results.get(0).getAgitUuid()).isEqualTo(agitUuid);
		assertThat(results.get(0).getVideoCount()).isEqualTo(1);
		assertThat(results.get(0).uploadedBy(userUuid)).isTrue();
		verify(topicReadCachePort).putLatestTopics(agitUuid, results);
	}

	@Test
	void listLatestByAgitUuid_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.listLatestByAgitUuid(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void listByAgitUuidAndStatus_queriesPersistenceWithKstTodayAndClampedLimit() {
		UUID agitUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Topic topic = Topic.create(agitUuid, UUID.randomUUID(), "오늘", today.atStartOfDay());
		given(topicPersistencePort.findByAgitUuidAndListStatus(agitUuid, TopicListStatus.ONGOING, today, 10))
				.willReturn(List.of(topic));

		var results = topicService.listByAgitUuidAndStatus(agitUuid, TopicListStatus.ONGOING, null);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getTitle()).isEqualTo("오늘");
		verify(topicReadCachePort, never()).getLatestTopics(any());
		verify(topicPersistencePort).findByAgitUuidAndListStatus(agitUuid, TopicListStatus.ONGOING, today, 10);
	}

	@Test
	void listByAgitUuidAndStatus_clampsLimitToTwenty() {
		UUID agitUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		given(topicPersistencePort.findByAgitUuidAndListStatus(agitUuid, TopicListStatus.PAST, today, 20))
				.willReturn(List.of());

		topicService.listByAgitUuidAndStatus(agitUuid, TopicListStatus.PAST, 21);

		verify(topicPersistencePort).findByAgitUuidAndListStatus(agitUuid, TopicListStatus.PAST, today, 20);
	}

	@Test
	void listByAgitUuidAndStatus_honorsLimitFive() {
		UUID agitUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		given(topicPersistencePort.findByAgitUuidAndListStatus(agitUuid, TopicListStatus.UPCOMING, today, 5))
				.willReturn(List.of());

		topicService.listByAgitUuidAndStatus(agitUuid, TopicListStatus.UPCOMING, 5);

		verify(topicPersistencePort).findByAgitUuidAndListStatus(agitUuid, TopicListStatus.UPCOMING, today, 5);
	}

	@Test
	void listByAgitUuidAndStatus_requiresAgitUuidAndStatus() {
		assertThatThrownBy(() -> topicService.listByAgitUuidAndStatus(null, TopicListStatus.ONGOING, 10))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
		assertThatThrownBy(() -> topicService.listByAgitUuidAndStatus(UUID.randomUUID(), null, 10))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("status는 필수입니다.");
	}

	@Test
	void get_returnsTopic() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.empty());
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		var result = topicService.get(topic.getTopicUuid());

		assertThat(result.getTitle()).isEqualTo("제목");
		assertThat(result.getVideoCount()).isZero();
		verify(topicViewerSnapshotPort).save(topic);
	}

	@Test
	void get_usesSnapshotWithoutPersistence() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));

		var result = topicService.get(topic.getTopicUuid());

		assertThat(result.getTitle()).isEqualTo("제목");
		verify(topicPersistencePort, never()).findByTopicUuid(any());
		verify(topicViewerSnapshotPort, never()).save(any(Topic.class));
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
		given(agitMembershipPort.findActiveMember(agitUuid, "Bearer test"))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(topicPersistencePort.save(any(Topic.class))).willAnswer(invocation -> invocation.getArgument(0));

		var result = topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(agitUuid)
				.title("점심 메뉴")
				.startAt(startAt)
				.build(), creatorUuid, "Bearer test");

		assertThat(result.getTopicUuid()).isNotNull();
		assertThat(result.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(result.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(result.getTitle()).isEqualTo("점심 메뉴");
		assertThat(result.getStartAt()).isEqualTo(startAt);
		assertThat(result.getVideoCount()).isZero();
		verify(topicPersistencePort).save(any(Topic.class));
		verify(topicReadCachePort).evict(agitUuid, startAt.toLocalDate());
		verify(topicViewerSnapshotPort).save(any(Topic.class));
		verify(topicCreatedEventPort).publishCreated(any(Topic.class));
		verify(topicAgitSyncEventPort).publishBoundAndStarted(any(Topic.class));
	}

	@Test
	void create_requiresAgitUuid() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.title("제목")
				.build(), UUID.randomUUID(), "Bearer test"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("agitUuid는 필수입니다.");
	}

	@Test
	void create_requiresActor() {
		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(UUID.randomUUID())
				.title("제목")
				.build(), null, "Bearer test"))
				.isInstanceOf(UnauthenticatedActorException.class);
	}

	@Test
	void create_forbidsNonMember() {
		UUID agitUuid = UUID.randomUUID();
		given(agitMembershipPort.findActiveMember(agitUuid, "Bearer test")).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicService.create(CreateTopicRequestDto.builder()
				.agitUuid(agitUuid)
				.title("제목")
				.build(), UUID.randomUUID(), "Bearer test"))
				.isInstanceOf(ForbiddenActorException.class);
		verify(topicPersistencePort, never()).save(any(Topic.class));
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
				.build(), existing.getCreatorUuid(), "Bearer test");

		assertThat(result.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(result.getStartAt()).isEqualTo(existing.getStartAt());
		assertThat(result.getTopicUuid()).isEqualTo(existing.getTopicUuid());
		verify(topicPersistencePort).update(any(Topic.class));
		verify(topicViewerSnapshotPort).save(any(Topic.class));
		verify(topicViewerSnapshotPort, never()).delete(any());
		verify(topicAgitSyncEventPort).publishBoundAndStarted(any(Topic.class));
		verify(topicCreatedEventPort, never()).publishCreated(any(Topic.class));
		verify(agitMembershipPort, never()).findActiveMember(any(), any());
	}

	@Test
	void update_allowsHostWhoIsNotCreator() {
		Topic existing = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);
		UUID hostUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));
		given(topicPersistencePort.update(any(Topic.class))).willAnswer(invocation -> invocation.getArgument(0));
		given(agitMembershipPort.findActiveMember(existing.getAgitUuid(), "Bearer test"))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.HOST)));

		var result = topicService.update(existing.getTopicUuid(), UpdateTopicRequestDto.builder()
				.title("저녁 메뉴")
				.build(), hostUuid, "Bearer test");

		assertThat(result.getTitle()).isEqualTo("저녁 메뉴");
	}

	@Test
	void update_forbidsGuestWhoIsNotCreator() {
		Topic existing = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));
		given(agitMembershipPort.findActiveMember(existing.getAgitUuid(), "Bearer test"))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		assertThatThrownBy(() -> topicService.update(existing.getTopicUuid(), UpdateTopicRequestDto.builder()
				.title("저녁 메뉴")
				.build(), UUID.randomUUID(), "Bearer test"))
				.isInstanceOf(ForbiddenActorException.class);
		verify(topicPersistencePort, never()).update(any(Topic.class));
	}

	@Test
	void update_throwsWhenMissing() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicService.update(topicUuid, UpdateTopicRequestDto.builder()
				.title("제목")
				.build(), UUID.randomUUID(), "Bearer test"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
		verify(topicAgitSyncEventPort, never()).publishBoundAndStarted(any(Topic.class));
		verify(topicViewerSnapshotPort, never()).delete(any());
		verify(topicViewerSnapshotPort, never()).save(any(Topic.class));
	}

	@Test
	void delete_softDeletesEmptyTopic() {
		Topic existing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));

		topicService.delete(existing.getTopicUuid(), existing.getCreatorUuid(), "Bearer test");

		verify(topicPersistencePort).deleteByTopicUuid(existing.getTopicUuid());
		verify(topicViewerSnapshotPort).delete(existing.getTopicUuid());
		verify(topicAgitSyncEventPort).publishUnbound(existing);
		verify(agitMembershipPort, never()).findActiveMember(any(), any());
	}

	@Test
	void delete_rejectsTopicWithVideos() {
		Topic existing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
				.attachVideo(UUID.randomUUID(), UUID.randomUUID());
		given(topicPersistencePort.findByTopicUuid(existing.getTopicUuid())).willReturn(Optional.of(existing));

		assertThatThrownBy(() -> topicService.delete(existing.getTopicUuid(), existing.getCreatorUuid(), "Bearer test"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("영상이 있는 토픽은 삭제할 수 없습니다.");
		verify(topicPersistencePort, never()).deleteByTopicUuid(existing.getTopicUuid());
		verify(topicViewerSnapshotPort, never()).delete(any());
		verify(topicViewerSnapshotPort, never()).save(any(Topic.class));
		verify(topicAgitSyncEventPort, never()).publishUnbound(any(Topic.class));
	}

	@Test
	void delete_throwsWhenMissing() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicService.delete(topicUuid, UUID.randomUUID(), "Bearer test"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
		verify(topicAgitSyncEventPort, never()).publishUnbound(any(Topic.class));
		verify(topicViewerSnapshotPort, never()).delete(any());
	}
}
