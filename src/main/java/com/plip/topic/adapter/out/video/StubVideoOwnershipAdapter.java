package com.plip.topic.adapter.out.video;

import com.plip.topic.application.exception.VideoOwnershipUnavailableException;
import com.plip.topic.application.port.out.VideoOwnershipPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class StubVideoOwnershipAdapter implements VideoOwnershipPort {

	private final Map<UUID, UUID> owners = new ConcurrentHashMap<>();
	private final Set<UUID> missingVideoUuids = ConcurrentHashMap.newKeySet();
	private boolean unavailable;

	public void setOwner(UUID videoUuid, UUID ownerUuid) {
		if (videoUuid != null && ownerUuid != null) {
			owners.put(videoUuid, ownerUuid);
		}
	}

	public void markMissing(UUID videoUuid) {
		if (videoUuid != null) {
			missingVideoUuids.add(videoUuid);
		}
	}

	public void markUnavailable() {
		unavailable = true;
	}

	public void reset() {
		owners.clear();
		missingVideoUuids.clear();
		unavailable = false;
	}

	@Override
	public Optional<UUID> findOwnerUuid(UUID videoUuid) {
		if (unavailable) {
			throw new VideoOwnershipUnavailableException();
		}
		if (videoUuid == null || missingVideoUuids.contains(videoUuid)) {
			return Optional.empty();
		}
		return Optional.ofNullable(owners.get(videoUuid));
	}
}
