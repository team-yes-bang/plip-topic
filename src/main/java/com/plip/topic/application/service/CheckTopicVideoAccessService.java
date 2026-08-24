package com.plip.topic.application.service;

import com.plip.topic.application.port.in.CheckTopicVideoAccessUseCase;
import com.plip.topic.application.port.in.TopicVideoAccessStatus;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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
			return TopicVideoAccessStatus.NOT_FOUND;
		}

		Optional<UUID> agitUuidOpt = topicPersistencePort.findAgitUuidByVideoUuid(videoUuid);
		if (agitUuidOpt.isEmpty()) {
			return TopicVideoAccessStatus.NOT_FOUND;
		}

		UUID agitUuid = agitUuidOpt.get();
		return agitMembershipPort.findActiveMember(agitUuid, userUuid)
				.map(membership -> TopicVideoAccessStatus.ALLOWED)
				.orElse(TopicVideoAccessStatus.FORBIDDEN);
	}
}
