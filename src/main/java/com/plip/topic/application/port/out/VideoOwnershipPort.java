package com.plip.topic.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface VideoOwnershipPort {

	Optional<UUID> findOwnerUuid(UUID videoUuid);
}
