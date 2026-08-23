package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class StubAgitMembershipAdapter implements AgitMembershipPort {

	private final Set<UUID> deniedUserUuids = ConcurrentHashMap.newKeySet();
	private final Map<UUID, AgitMemberRole> roles = new ConcurrentHashMap<>();

	public void deny(UUID userUuid) {
		if (userUuid != null) {
			deniedUserUuids.add(userUuid);
		}
	}

	public void setRole(UUID userUuid, AgitMemberRole role) {
		if (userUuid != null && role != null) {
			roles.put(userUuid, role);
		}
	}

	public void reset() {
		deniedUserUuids.clear();
		roles.clear();
	}

	@Override
	public Optional<AgitMembership> findActiveMember(UUID agitUuid, UUID userUuid) {
		if (userUuid == null || deniedUserUuids.contains(userUuid)) {
			return Optional.empty();
		}
		AgitMemberRole role = roles.getOrDefault(userUuid, AgitMemberRole.HOST);
		return Optional.of(new AgitMembership(role));
	}
}
