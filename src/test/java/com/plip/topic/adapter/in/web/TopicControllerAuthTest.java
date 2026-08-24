package com.plip.topic.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class TopicControllerAuthTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void create_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(post("/api/v1/topics")
				.contentType(APPLICATION_JSON)
				.content("""
						{"agitUuid":"%s","title":"제목"}
						""".formatted(UUID.randomUUID())));
	}

	@Test
	void list_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics").param("agitUuid", UUID.randomUUID().toString()));
	}

	@Test
	void listByStatus_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics/list")
				.param("agitUuid", UUID.randomUUID().toString())
				.param("status", "ONGOING"));
	}

	@Test
	void feed_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics/feed")
				.param("agitUuid", UUID.randomUUID().toString())
				.param("date", "2026-08-24"));
	}

	@Test
	void calendar_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics/calendar")
				.param("agitUuid", UUID.randomUUID().toString())
				.param("yearMonth", "2026-08"));
	}

	@Test
	void get_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics/{topicUuid}", UUID.randomUUID()));
	}

	@Test
	void update_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(patch("/api/v1/topics/{topicUuid}", UUID.randomUUID())
				.contentType(APPLICATION_JSON)
				.content("{\"title\":\"저녁 메뉴\"}"));
	}

	@Test
	void delete_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(delete("/api/v1/topics/{topicUuid}", UUID.randomUUID()));
	}

	@Test
	void listVideos_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(get("/api/v1/topics/{topicUuid}/videos", UUID.randomUUID()));
	}

	@Test
	void attachVideo_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(post("/api/v1/topics/{topicUuid}/videos", UUID.randomUUID())
				.contentType(APPLICATION_JSON)
				.content("{\"videoUuid\":\"%s\"}".formatted(UUID.randomUUID())));
	}

	@Test
	void detachVideo_returnsUnauthorizedWithoutActor() throws Exception {
		expectUnauthorized(delete(
				"/api/v1/topics/{topicUuid}/videos/{videoUuid}",
				UUID.randomUUID(),
				UUID.randomUUID()
		));
	}

	@Test
	void health_remainsPublic() throws Exception {
		mockMvc.perform(get("/api/test"))
				.andExpect(status().isOk());
	}

	private void expectUnauthorized(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
			throws Exception {
		mockMvc.perform(request)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("ACCESS_TOKEN_INVALID"))
				.andExpect(jsonPath("$.message").value("인증이 필요합니다."));
	}
}
