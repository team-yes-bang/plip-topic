package com.plip.topic.application.exception;

public class VideoOwnershipUnavailableException extends RuntimeException {

	public VideoOwnershipUnavailableException() {
		super("영상 소유권 조회에 실패했습니다.");
	}

	public VideoOwnershipUnavailableException(Throwable cause) {
		super("영상 소유권 조회에 실패했습니다.", cause);
	}
}
