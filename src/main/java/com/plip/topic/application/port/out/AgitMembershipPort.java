package com.plip.topic.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface AgitMembershipPort {

	Optional<AgitMembership> findActiveMember(UUID agitUuid, UUID userUuid);
}
