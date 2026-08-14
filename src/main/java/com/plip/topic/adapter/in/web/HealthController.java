package com.plip.topic.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Tag(name = "Health", description = "서비스 기동 확인")
public class HealthController {

	@Operation(summary = "로컬 기동 확인")
	@GetMapping("/api/test")
	public String test() {
		log.info("topic health check");
		return "test success!";
	}
}
