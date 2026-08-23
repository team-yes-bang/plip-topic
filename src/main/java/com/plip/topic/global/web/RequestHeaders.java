package com.plip.topic.global.web;

/**
 * Gateway·타 서비스 연동에 공통으로 쓰는 HTTP 헤더 상수.
 * <p>클라이언트는 {@code Authorization: Bearer}로 JWT를 전달하고,
 * Gateway가 검증 후 본 헤더를 downstream에 주입한다.</p>
 */
public final class RequestHeaders {

	/** plip-gateway {@code UserUuidHeader#NAME} 와 동일해야 한다. */
	public static final String USER_UUID_HEADER = "X-User-UUID";

	private RequestHeaders() {
	}
}
