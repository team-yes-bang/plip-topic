package com.plip.topic.application.service;

import com.plip.topic.application.port.in.CheckTopicVideoAccessUseCase;
import com.plip.topic.application.port.in.TopicVideoAccessStatus;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckTopicVideoAccessService implements CheckTopicVideoAccessUseCase {

	private final TopicPersistencePort topicPersistencePort;
	private final AgitMembershipPort agitMembershipPort;

	@Override
	public TopicVideoAccessStatus checkAccess(UUID videoUuid, UUID userUuid) {
		if (videoUuid == null || userUuid == null) {
			return TopicVideoAccessStatus.FORBIDDEN;
		}

		List<UUID> agitUuids = topicPersistencePort.findAgitUuidsByVideoUuid(videoUuid);
		if (agitUuids.isEmpty()) {
			return TopicVideoAccessStatus.FORBIDDEN;
		}

		for (UUID agitUuid : agitUuids) {
			if (agitMembershipPort.findActiveMember(agitUuid, userUuid).isPresent()) {
				return TopicVideoAccessStatus.ALLOWED;
			}
		}
		return TopicVideoAccessStatus.FORBIDDEN;
	}
}
