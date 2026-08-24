package com.plip.topic.adapter.out.persistence;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicListStatus;
import com.plip.topic.domain.model.TopicVideoLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
				Topic.create(agitUuid, creatorUuid, "주말 모임", startAt).attachVideo(creatorUuid, videoUuid)
		);

		Topic found = topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow();

		assertThat(found.getTopicUuid()).isEqualTo(saved.getTopicUuid());
		assertThat(found.getAgitUuid()).isEqualTo(agitUuid);
		assertThat(found.getCreatorUuid()).isEqualTo(creatorUuid);
		assertThat(found.getTitle()).isEqualTo("주말 모임");
		assertThat(found.getStartAt()).isEqualTo(startAt);
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getVideos()).extracting(video -> video.getVideoUuid()).containsExactly(videoUuid);
		assertThat(found.getVideos().get(0).getUserUuid()).isEqualTo(creatorUuid);
		assertThat(found.isDeleted()).isFalse();
	}

	@Test
	void update_changesTitleAndKeepsStartAt() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "점심 메뉴", LocalDateTime.of(2026, 8, 18, 0, 0))
		);

		Topic updated = saved.update("저녁 메뉴", null);
		Topic found = topicPersistencePort.update(updated);

		assertThat(found.getTopicUuid()).isEqualTo(saved.getTopicUuid());
		assertThat(found.getTitle()).isEqualTo("저녁 메뉴");
		assertThat(found.getStartAt()).isEqualTo(saved.getStartAt());
	}

	@Test
	void update_throwsWhenMissing() {
		Topic missing = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null);

		assertThatThrownBy(() -> topicPersistencePort.update(missing))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void deleteByTopicUuid_hidesTopicFromFind() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "삭제 대상", null)
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
	void findByAgitUuidAndListStatus_classifiesByDateExcludesDeletedAndIncludesEmpty() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "어제", today.minusDays(1).atTime(18, 0)));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "그저께", today.minusDays(2).atStartOfDay()));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-빈", today.atStartOfDay()));
		Topic todayWithVideo = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "오늘-영상", today.atTime(12, 0)));
		topicPersistencePort.addVideoIfAbsent(todayWithVideo.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "내일", today.plusDays(1).atStartOfDay()));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "모레", today.plusDays(2).atStartOfDay()));
		Topic deleted = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "삭제", today.atStartOfDay()));
		topicPersistencePort.deleteByTopicUuid(deleted.getTopicUuid());
		topicPersistencePort.save(Topic.create(UUID.randomUUID(), creatorUuid, "다른아지트", today.atStartOfDay()));

		List<Topic> ongoing = topicPersistencePort.findByAgitUuidAndListStatus(
				agitUuid, TopicListStatus.ONGOING, today, 10);
		List<Topic> upcoming = topicPersistencePort.findByAgitUuidAndListStatus(
				agitUuid, TopicListStatus.UPCOMING, today, 10);
		List<Topic> past = topicPersistencePort.findByAgitUuidAndListStatus(
				agitUuid, TopicListStatus.PAST, today, 10);

		assertThat(ongoing).extracting(Topic::getTitle).containsExactly("오늘-빈", "오늘-영상");
		assertThat(ongoing.get(0).videoCount()).isZero();
		assertThat(upcoming).extracting(Topic::getTitle).containsExactly("내일", "모레");
		assertThat(past).extracting(Topic::getTitle).containsExactly("어제", "그저께");
		assertThat(ongoing).extracting(Topic::getTitle).doesNotContain("삭제", "다른아지트");
	}

	@Test
	void findByAgitUuidAndListStatus_capsAtLimit() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		for (int i = 1; i <= 6; i++) {
			topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "u-" + i, today.plusDays(i).atStartOfDay()));
		}

		List<Topic> found = topicPersistencePort.findByAgitUuidAndListStatus(
				agitUuid, TopicListStatus.UPCOMING, today, 5);

		assertThat(found).hasSize(5);
		assertThat(found).extracting(Topic::getTitle).containsExactly("u-1", "u-2", "u-3", "u-4", "u-5");
	}

	@Test
	void findLatestByAgitUuid_returnsNewestStartAtFirstAndCapsAtTen() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		topicPersistencePort.save(Topic.create(
				agitUuid, creatorUuid, "old", LocalDateTime.of(2026, 8, 1, 0, 0)));
		for (int day = 2; day <= 12; day++) {
			topicPersistencePort.save(Topic.create(
					agitUuid, creatorUuid, "day-" + day, LocalDateTime.of(2026, 8, day, 0, 0)));
		}
		topicPersistencePort.save(Topic.create(
				UUID.randomUUID(), creatorUuid, "other", LocalDateTime.of(2026, 8, 20, 0, 0)));

		List<Topic> found = topicPersistencePort.findLatestByAgitUuid(agitUuid, 10);

		assertThat(found).hasSize(10);
		assertThat(found.get(0).getTitle()).isEqualTo("day-12");
		assertThat(found.get(9).getTitle()).isEqualTo("day-3");
		assertThat(found).extracting(Topic::getTitle).doesNotContain("old", "other");
	}

	@Test
	void findFeedOngoingWithVideos_includesEmptyTodayAndExcludesUpcomingAndDeleted() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-빈", today.atStartOfDay()));
		Topic first = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-1", today.atTime(8, 0)));
		Topic second = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-2", today.atTime(12, 0)));
		topicPersistencePort.addVideoIfAbsent(first.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(second.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		Topic upcoming = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "내일", today.plusDays(1).atStartOfDay()));
		topicPersistencePort.addVideoIfAbsent(upcoming.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		Topic deleted = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "삭제", today.atTime(9, 0)));
		topicPersistencePort.addVideoIfAbsent(deleted.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.deleteByTopicUuid(deleted.getTopicUuid());

		List<Topic> ongoing = topicPersistencePort.findFeedOngoingWithVideos(agitUuid, today);

		assertThat(ongoing).extracting(Topic::getTitle).containsExactly("오늘-빈", "오늘-1", "오늘-2");
		assertThat(ongoing.get(0).videoCount()).isZero();
	}

	@Test
	void findFeedPastFromStart_ordersRecentPastFirstAndSkipsEmpty() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Topic yesterday = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "어제", today.minusDays(1).atStartOfDay()));
		Topic older = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "그저께", today.minusDays(2).atStartOfDay()));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "빈-어제", today.minusDays(1).atTime(12, 0)));
		topicPersistencePort.addVideoIfAbsent(yesterday.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(older.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());

		List<Topic> past = topicPersistencePort.findFeedPastFromStart(agitUuid, today, 10);

		assertThat(past).extracting(Topic::getTitle).containsExactly("어제", "그저께");
	}

	@Test
	void findFeedAnchorOnDate_picksTodayFirstOrLatestPastDay() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Topic todayFirst = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "오늘-첫", today.atTime(8, 0)));
		Topic todaySecond = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "오늘-다음", today.atTime(18, 0)));
		Topic pastMorning = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "어제-아침", today.minusDays(1).atTime(8, 0)));
		Topic pastEvening = topicPersistencePort.save(
				Topic.create(agitUuid, creatorUuid, "어제-저녁", today.minusDays(1).atTime(20, 0)));
		topicPersistencePort.addVideoIfAbsent(todayFirst.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(todaySecond.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(pastMorning.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(pastEvening.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());

		assertThat(topicPersistencePort.findFeedAnchorOnDate(agitUuid, today, today))
				.map(Topic::getTitle)
				.contains("오늘-첫");
		assertThat(topicPersistencePort.findFeedAnchorOnDate(agitUuid, today, today.minusDays(1)))
				.map(Topic::getTitle)
				.contains("어제-저녁");
		assertThat(topicPersistencePort.findFeedAnchorOnDate(agitUuid, today, today.plusDays(1))).isEmpty();
	}

	@Test
	void findActiveDates_returnsDaysWithVideosOnly() {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		topicPersistencePort.save(Topic.create(
				agitUuid, creatorUuid, "영상", LocalDateTime.of(2026, 8, 14, 0, 0))
				.attachVideo(creatorUuid, UUID.randomUUID()));
		topicPersistencePort.save(Topic.create(
				agitUuid, creatorUuid, "빈 토픽", LocalDateTime.of(2026, 8, 15, 0, 0)));

		List<java.time.LocalDate> dates = topicPersistencePort.findActiveDates(agitUuid, java.time.YearMonth.of(2026, 8));

		assertThat(dates).containsExactly(java.time.LocalDate.of(2026, 8, 14));
	}

	@Test
	void addVideoIfAbsent_appendsNewVideo() {
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null)
		);
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();

		boolean attached = topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), videoUuid, userUuid);
		Topic found = topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow();

		assertThat(attached).isTrue();
		assertThat(found.getVideos()).extracting(video -> video.getVideoUuid()).containsExactly(videoUuid);
		assertThat(found.getVideos().get(0).getUserUuid()).isEqualTo(userUuid);
	}

	@Test
	void addVideoIfAbsent_isIdempotent() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null).attachVideo(userUuid, videoUuid)
		);

		boolean attached = topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), videoUuid, userUuid);

		assertThat(attached).isFalse();
		assertThat(topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow().getVideos())
				.hasSize(1);
	}

	@Test
	void addVideoIfAbsent_rejectsSecondVideoFromSameUser() {
		UUID userUuid = UUID.randomUUID();
		Topic saved = topicPersistencePort.save(
				Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", null).attachVideo(userUuid, UUID.randomUUID())
		);

		assertThatThrownBy(() -> topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), UUID.randomUUID(), userUuid))
				.isInstanceOf(TopicVideoLimitException.class);
	}

	@Test
	void addVideoIfAbsent_returnsFalseWhenTopicMissing() {
		assertThat(topicPersistencePort.addVideoIfAbsent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())).isFalse();
	}

	@Test
	void addVideoIfAbsent_activatesCalendarDay() {
		UUID agitUuid = UUID.randomUUID();
		Topic saved = topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"빈 토픽",
				java.time.LocalDateTime.of(2026, 8, 18, 0, 0)
		));

		assertThat(topicPersistencePort.findActiveDates(agitUuid, java.time.YearMonth.of(2026, 8))).isEmpty();

		topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());

		assertThat(topicPersistencePort.findActiveDates(agitUuid, java.time.YearMonth.of(2026, 8)))
				.containsExactly(java.time.LocalDate.of(2026, 8, 18));
	}

	@Test
	void removeVideo_hidesFromFindAndDeactivatesCalendarWhenLast() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic saved = topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"제목",
				java.time.LocalDateTime.of(2026, 8, 18, 0, 0)
		).attachVideo(userUuid, videoUuid));

		topicPersistencePort.removeVideo(saved.getTopicUuid(), videoUuid, userUuid);

		assertThat(topicPersistencePort.findByTopicUuid(saved.getTopicUuid()).orElseThrow().getVideos()).isEmpty();
		assertThat(topicPersistencePort.findActiveDates(agitUuid, java.time.YearMonth.of(2026, 8))).isEmpty();
	}
}
