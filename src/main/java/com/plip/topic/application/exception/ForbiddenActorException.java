package com.plip.topic.application.exception;

public class ForbiddenActorException extends RuntimeException {

	public ForbiddenActorException() {
		super("권한이 없습니다.");
	}
}
