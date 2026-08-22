package com.plip.topic.application.exception;

public class AgitMembershipUnavailableException extends RuntimeException {

	public AgitMembershipUnavailableException() {
		super("아지트 권한 조회에 실패했습니다.");
	}

	public AgitMembershipUnavailableException(Throwable cause) {
		super("아지트 권한 조회에 실패했습니다.", cause);
	}
}
