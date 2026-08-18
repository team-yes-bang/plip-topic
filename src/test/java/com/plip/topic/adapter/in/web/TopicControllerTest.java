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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
				"grid",
				List.of()
		));
		topicPersistencePort.save(Topic.create(
				agitUuid,
				UUID.randomUUID(),
				"최근 모임",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				"grid",
				List.of(videoUuid)
		));
		topicPersistencePort.save(Topic.create(
				otherAgitUuid,
				UUID.randomUUID(),
				"다른 아지트",
				LocalDateTime.of(2026, 8, 14, 0, 0),
				"grid",
				List.of()
		));

		mockMvc.perform(get("/api/v1/topics").param("agitUuid", agitUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].title").value("최근 모임"))
				.andExpect(jsonPath("$[0].videoUuids[0]").value(videoUuid.toString()))
				.andExpect(jsonPath("$[1].title").value("이전 모임"))
				.andExpect(jsonPath("$[0].agitUuid").value(agitUuid.toString()));
	}

	@Test
	void list_returnsEmptyArrayWhenNoTopics() throws Exception {
		mockMvc.perform(get("/api/v1/topics").param("agitUuid", UUID.randomUUID().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
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
								  "layout": "grid",
								  "videoUuids": ["%s"]
								}
								""".formatted(agitUuid, creatorUuid, videoUuid)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.topicUuid").exists())
				.andExpect(jsonPath("$.agitUuid").value(agitUuid.toString()))
				.andExpect(jsonPath("$.creatorUuid").value(creatorUuid.toString()))
				.andExpect(jsonPath("$.title").value("점심 메뉴"))
				.andExpect(jsonPath("$.layout").value("grid"))
				.andExpect(jsonPath("$.videoUuids[0]").value(videoUuid.toString()));

		mockMvc.perform(get("/api/v1/topics").param("agitUuid", agitUuid.toString()))
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
}
