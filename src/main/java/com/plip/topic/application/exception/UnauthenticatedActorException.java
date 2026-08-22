package com.plip.topic.application.exception;

public class UnauthenticatedActorException extends RuntimeException {

	public UnauthenticatedActorException() {
		super("인증된 사용자가 없습니다.");
	}
}
