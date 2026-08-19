package com.plip.topic.adapter.in.web;

import com.plip.topic.domain.model.TopicVideoLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class WebExceptionHandler {

	@ExceptionHandler(TopicVideoLimitException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Map<String, String> handleTopicVideoLimit(TopicVideoLimitException exception) {
		return Map.of("message", exception.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> handleIllegalArgument(IllegalArgumentException exception) {
		return Map.of("message", exception.getMessage());
	}
}
