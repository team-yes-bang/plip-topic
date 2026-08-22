package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("test")
public class StubAgitMembershipAdapter implements AgitMembershipPort {

	@Override
	public Optional<AgitMembership> findActiveMember(UUID agitUuid, String authorization) {
		return Optional.of(new AgitMembership(AgitMemberRole.HOST));
	}
}
