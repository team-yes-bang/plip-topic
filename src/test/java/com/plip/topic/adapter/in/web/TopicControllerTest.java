package com.plip.topic.adapter.in.web;

import com.plip.topic.adapter.out.agit.StubAgitMembershipAdapter;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.global.web.RequestHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TopicControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TopicPersistencePort topicPersistencePort;

	@Autowired
	private StubAgitMembershipAdapter stubAgitMembershipAdapter;

	private UUID viewerUuid;

	@BeforeEach
	void resetMembershipStub() {
		stubAgitMembershipAdapter.reset();
		viewerUuid = UUID.randomUUID();
	}

	private static RequestPostProcessor actor(UUID userUuid) {
		return request -> {
			request.addHeader(RequestHeaders.USER_UUID_HEADER, userUuid.toString());
			return request;
		};
	}

	@Test
	void list_returnsLatestTopicsByStartAt() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID otherAgitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();

		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"이전 모임",
				LocalDateTime.of(2026, 1, 1, 0, 0)
		));
		Topic withVideo = topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"최근 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0)
		));
		topicPersistencePort.addVideoIfAbsent(withVideo.getTopicUuid(), videoUuid, userUuid);
		topicPersistencePort.save(Topic.create(
				otherAgitUuid,
				UUID.randomUUID(),
				"다른 아지트",
				LocalDateTime.of(2026, 8, 20, 0, 0)
		));

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", agitUuid.toString())
						.with(actor(userUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("최근 모임"))
				.andExpect(jsonPath("$[0].videoCount").value(1))
				.andExpect(jsonPath("$[0].uploadedByMe").value(true))
				.andExpect(jsonPath("$[1].title").value("이전 모임"))
				.andExpect(jsonPath("$[0].videoUuids").doesNotExist())
				.andExpect(jsonPath("$[0].agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$[0].layout").doesNotExist());
	}

	@Test
	void list_returnsEmptyArrayWhenNoTopics() throws Exception {
		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", UUID.randomUUID().toString()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void list_requiresAgitUuid() throws Exception {
		mockMvc.perform(get("/api/v1/topics").with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listByStatus_classifiesByKstDateAndKeepsLatestListUnchanged() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "어제", today.minusDays(1).atStartOfDay()));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘", today.atTime(23, 0)));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "내일", today.plusDays(1).atStartOfDay()));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("오늘"))
				.andExpect(jsonPath("$[0].uploadedByMe").value(false));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "UPCOMING").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("내일"));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "PAST").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("어제"));

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", agitUuid.toString()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].title").value("내일"))
				.andExpect(jsonPath("$[1].title").value("오늘"))
				.andExpect(jsonPath("$[2].title").value("어제"));
	}

	@Test
	void listByStatus_requiresStatus() throws Exception {
		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", UUID.randomUUID().toString()).with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listByStatus_requiresAgitUuid() throws Exception {
		mockMvc.perform(get("/api/v1/topics/list")
						.param("status", "ONGOING").with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listByStatus_rejectsUnknownStatus() throws Exception {
		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", UUID.randomUUID().toString())
						.param("status", "ALL").with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listByStatus_defaultsLimitToTenAndClampsToTwenty() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDateTime todayStart = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
		for (int i = 1; i <= 21; i++) {
			topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "today-" + i, todayStart.plusMinutes(i)));
		}

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING")
						.param("limit", "21").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(20));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING")
						.param("limit", "5").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(5));
	}

	@Test
	void listByStatus_excludesDeletedAndIncludesZeroVideos() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDateTime todayStart = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "빈 토픽", todayStart));
		Topic withVideo = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "영상 토픽", todayStart.plusHours(1)));
		topicPersistencePort.addVideoIfAbsent(withVideo.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		Topic deleted = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "삭제", todayStart.plusHours(2)));
		topicPersistencePort.deleteByTopicUuid(deleted.getTopicUuid());

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("빈 토픽"))
				.andExpect(jsonPath("$[0].videoCount").value(0))
				.andExpect(jsonPath("$[1].title").value("영상 토픽"))
				.andExpect(jsonPath("$[1].videoCount").value(1));
	}

	@Test
	void feed_returnsNeighborsAndKeepsListContract() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Topic todayFirst = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-1", today.atTime(8, 0)));
		Topic todaySecond = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-2", today.atTime(12, 0)));
		Topic yesterday = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "어제", today.minusDays(1).atStartOfDay()));
		topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "오늘-빈", today.atTime(9, 0)));
		Topic upcoming = topicPersistencePort.save(Topic.create(agitUuid, creatorUuid, "내일", today.plusDays(1).atStartOfDay()));
		topicPersistencePort.addVideoIfAbsent(todayFirst.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(todaySecond.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(yesterday.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.addVideoIfAbsent(upcoming.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());

		mockMvc.perform(get("/api/v1/topics/feed")
						.param("agitUuid", agitUuid.toString())
						.param("topicUuid", todaySecond.getTopicUuid().toString()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.current.title").value("오늘-2"))
				.andExpect(jsonPath("$.before.length()").value(1))
				.andExpect(jsonPath("$.before[0].title").value("오늘-빈"))
				.andExpect(jsonPath("$.before[0].videoCount").value(0))
				.andExpect(jsonPath("$.after.length()").value(1))
				.andExpect(jsonPath("$.after[0].title").value("어제"));

		mockMvc.perform(get("/api/v1/topics/feed")
						.param("agitUuid", agitUuid.toString())
						.param("date", today.toString()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.current.title").value("오늘-1"))
				.andExpect(jsonPath("$.after[0].title").value("오늘-빈"));

		mockMvc.perform(get("/api/v1/topics/list")
						.param("agitUuid", agitUuid.toString())
						.param("status", "ONGOING").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	void feed_requiresExactlyOneAnchor() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		mockMvc.perform(get("/api/v1/topics/feed")
						.param("agitUuid", agitUuid.toString()).with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/v1/topics/feed")
						.param("agitUuid", agitUuid.toString())
						.param("topicUuid", UUID.randomUUID().toString())
						.param("date", LocalDate.now(ZoneId.of("Asia/Seoul")).toString()).with(actor(viewerUuid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void get_returnsSingleTopic() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"단건",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));

		mockMvc.perform(get("/api/v1/topics/{topicUuid}", saved.getTopicUuid()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("단건"))
				.andExpect(jsonPath("$.videoCount").value(0))
				.andExpect(jsonPath("$.uploadedByMe").value(false));
	}

	@Test
	void create_returnsCreatedTopicWithoutVideos() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/topics")
						.with(actor(creatorUuid))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "agitUuid": "%s",
								  "title": "점심 메뉴",
								  "startAt": "2026-08-18T00:00:00"
								}
								""".formatted(agitUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.topicUuid").exists())
				.andExpect(jsonPath("$.agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$.creatorUuid").value(creatorUuid.toString()))
				.andExpect(jsonPath("$.title").value("점심 메뉴"))
				.andExpect(jsonPath("$.videoCount").value(0))
				.andExpect(jsonPath("$.videoUuids").doesNotExist())
				.andExpect(jsonPath("$.layout").doesNotExist());
	}

	@Test
	void attachVideo_returnsCreatedAndListsOnGrid() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/topics/{topicUuid}/videos", saved.getTopicUuid())
						.with(actor(userUuid))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "videoUuid": "%s"
								}
								""".formatted(videoUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.videoUuid").value(videoUuid.toString()))
				.andExpect(jsonPath("$.userUuid").value(userUuid.toString()));

		mockMvc.perform(get("/api/v1/topics/{topicUuid}/videos", saved.getTopicUuid()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].videoUuid").value(videoUuid.toString()));
	}

	@Test
	void attachVideo_returnsConflictWhenUserAlreadyUploaded() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		UUID userUuid = UUID.randomUUID();
		topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), UUID.randomUUID(), userUuid);

		mockMvc.perform(post("/api/v1/topics/{topicUuid}/videos", saved.getTopicUuid())
						.with(actor(userUuid))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "videoUuid": "%s"
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("이미 이 토픽에 영상을 올렸습니다."));
	}

	@Test
	void detachVideo_returnsNoContent() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), videoUuid, userUuid);

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}/videos/{videoUuid}", saved.getTopicUuid(), videoUuid)
						.with(actor(userUuid)))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/topics/{topicUuid}/videos", saved.getTopicUuid()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void create_requiresAgitUuid() throws Exception {
		mockMvc.perform(post("/api/v1/topics")
						.with(actor(UUID.randomUUID()))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "제목"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("agitUuid는 필수입니다."));
	}

	@Test
	void update_changesTitleAndKeepsStartAt() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));

		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(saved.getCreatorUuid()))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "저녁 메뉴"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.topicUuid").value(saved.getTopicUuid().toString()))
				.andExpect(jsonPath("$.title").value("저녁 메뉴"))
				.andExpect(jsonPath("$.startAt").value("2026-08-18T00:00:00"))
				.andExpect(jsonPath("$.layout").doesNotExist());
	}

	@Test
	void update_returnsBadRequestWhenMissing() throws Exception {
		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", UUID.randomUUID())
						.with(actor(UUID.randomUUID()))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "제목"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("토픽이 존재하지 않습니다."));
	}

	@Test
	void delete_returnsNoContentWhenEmpty() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"삭제 대상",
				null
		));

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(saved.getCreatorUuid())))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", saved.getAgitUuid().toString()).with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void calendar_returnsDatesWithVideosOnly() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		Topic withVideo = topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"영상 토픽",
				LocalDateTime.of(2026, 8, 14, 0, 0)
		));
		topicPersistencePort.addVideoIfAbsent(withVideo.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());
		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"빈 토픽",
				LocalDateTime.of(2026, 8, 15, 0, 0)
		));

		mockMvc.perform(get("/api/v1/topics/calendar")
						.param("agitUuid", agitUuid.toString())
						.param("yearMonth", "2026-08").with(actor(viewerUuid)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.yearMonth").value("2026-08"))
				.andExpect(jsonPath("$.activeDates.length()").value(1))
				.andExpect(jsonPath("$.activeDates[0]").value("2026-08-14"));
	}

	@Test
	void delete_returnsBadRequestWhenTopicHasVideos() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"영상 있는 토픽",
				null
		));
		topicPersistencePort.addVideoIfAbsent(saved.getTopicUuid(), UUID.randomUUID(), UUID.randomUUID());

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(saved.getCreatorUuid())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("영상이 있는 토픽은 삭제할 수 없습니다."));
	}

	@Test
	void delete_returnsBadRequestWhenMissing() throws Exception {
		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", UUID.randomUUID())
						.with(actor(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("토픽이 존재하지 않습니다."));
	}

	@Test
	void create_returnsUnauthorizedWithoutActor() throws Exception {
		mockMvc.perform(post("/api/v1/topics")
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "agitUuid": "%s",
								  "title": "제목"
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"))
				.andExpect(jsonPath("$.message").value("인증이 필요합니다."));
	}

	@Test
	void list_returnsUnauthorizedWithoutActor() throws Exception {
		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", UUID.randomUUID().toString()))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"));
	}

	@Test
	void list_returnsForbiddenForNonMember() throws Exception {
		stubAgitMembershipAdapter.deny(viewerUuid);
		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", UUID.randomUUID().toString())
						.with(actor(viewerUuid)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("권한이 없습니다."));
	}

	@Test
	void get_returnsForbiddenForNonMember() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"단건",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		stubAgitMembershipAdapter.deny(viewerUuid);
		mockMvc.perform(get("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(viewerUuid)))
				.andExpect(status().isForbidden());
	}

	@Test
	void update_allowsHostWhoIsNotCreator() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		UUID hostUuid = UUID.randomUUID();
		stubAgitMembershipAdapter.setRole(hostUuid, AgitMemberRole.HOST);

		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(hostUuid))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "저녁 메뉴"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("저녁 메뉴"));
	}

	@Test
	void update_forbidsGuestWhoIsNotCreator() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		UUID guestUuid = UUID.randomUUID();
		stubAgitMembershipAdapter.setRole(guestUuid, AgitMemberRole.GUEST);

		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(guestUuid))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "저녁 메뉴"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void update_forbidsWithdrawnCreator() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		));
		stubAgitMembershipAdapter.deny(saved.getCreatorUuid());

		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(saved.getCreatorUuid()))
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "title": "저녁 메뉴"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void delete_forbidsWithdrawnCreator() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"삭제 대상",
				null
		));
		stubAgitMembershipAdapter.deny(saved.getCreatorUuid());

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
						.with(actor(saved.getCreatorUuid())))
				.andExpect(status().isForbidden());
	}
}
