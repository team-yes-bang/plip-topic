package com.plip.topic.application.port.out;

public record AgitMembership(AgitMemberRole role) {

	public boolean isHost() {
		return role == AgitMemberRole.HOST;
	}
}
