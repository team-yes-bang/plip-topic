package com.plip.topic.adapter.in.web;

import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.domain.model.Topic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

	@Test
	void list_returnsTopicsOfAgitOrderedByStartAtDesc() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID otherAgitUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();

		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"이전 모임",
				LocalDateTime.of(2026, 1, 1, 0, 0),
				List.of()
		));
		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"최근 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				List.of(videoUuid)
		));
		topicPersistencePort.save(Topic.create(
				otherAgitUuid,
				UUID.randomUUID(),
				"다른 아지트",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				List.of()
		));

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", agitUuid.toString())
						.param("date", "2026-08-14"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("최근 모임"))
				.andExpect(jsonPath("$[0].videoUuids[0]").value(videoUuid.toString()))
				.andExpect(jsonPath("$[0].agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$[0].layout").doesNotExist());
	}

	@Test
	void list_returnsEmptyArrayWhenNoTopics() throws Exception {
		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", UUID.randomUUID().toString())
						.param("date", "2026-08-14"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void list_requiresDate() throws Exception {
		mockMvc.perform(get("/api/v1/topics").param("agitUuid", UUID.randomUUID().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void list_requiresAgitUuid() throws Exception {
		mockMvc.perform(get("/api/v1/topics"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_returnsCreatedTopic() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		UUID creatorUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/topics")
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "agitUuid": "%s",
								  "creatorUuid": "%s",
								  "title": "점심 메뉴",
								  "startAt": "2026-08-18T00:00:00",
								  "videoUuids": ["%s"]
								}
								""".formatted(agitUuid, creatorUuid, videoUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.topicUuid").exists())
				.andExpect(jsonPath("$.agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$.creatorUuid").value(creatorUuid.toString()))
				.andExpect(jsonPath("$.title").value("점심 메뉴"))
				.andExpect(jsonPath("$.layout").doesNotExist())
				.andExpect(jsonPath("$.videoUuids[0]").value(videoUuid.toString()));

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", agitUuid.toString())
						.param("date", "2026-08-18"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].title").value("점심 메뉴"));
	}

	@Test
	void create_requiresAgitUuid() throws Exception {
		mockMvc.perform(post("/api/v1/topics")
						.contentType(APPLICATION_JSON)
						.content("""
								{
								  "creatorUuid": "%s",
								  "title": "제목"
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("agitUuid는 필수입니다."));
	}

	@Test
	void update_changesTitleAndKeepsStartAt() throws Exception {
		Topic saved = topicPersistencePort.save(Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"점심 메뉴",
				LocalDateTime.of(2026, 8, 18, 0, 0),
				List.of()
		));

		mockMvc.perform(patch("/api/v1/topics/{topicUuid}", saved.getTopicUuid())
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
				null,
				List.of()
		));

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", saved.getTopicUuid()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/topics")
						.param("agitUuid", saved.getAgitUuid().toString())
						.param("date", LocalDate.now().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void calendar_returnsDatesWithVideosOnly() throws Exception {
		UUID agitUuid = UUID.randomUUID();
		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"영상 토픽",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				List.of(UUID.randomUUID())
		));
		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"빈 토픽",
				LocalDateTime.of(2026, 8, 15, 0, 0),
				List.of()
		));

		mockMvc.perform(get("/api/v1/topics/calendar")
						.param("agitUuid", agitUuid.toString())
						.param("yearMonth", "2026-08"))
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
				null,
				List.of(UUID.randomUUID())
		));

		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", saved.getTopicUuid()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("영상이 있는 토픽은 삭제할 수 없습니다."));
	}

	@Test
	void delete_returnsBadRequestWhenMissing() throws Exception {
		mockMvc.perform(delete("/api/v1/topics/{topicUuid}", UUID.randomUUID()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("토픽이 존재하지 않습니다."));
	}
}
