package com.plip.topic.adapter.in.web;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Hidden
public class HealthController {

	@GetMapping("/api/test")
	public String test() {
		log.info("topic health check");
		return "test success!";
	}
}
