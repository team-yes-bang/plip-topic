package com.plip.topic.application.service;

import com.plip.topic.application.port.in.TopicVideoAccessStatus;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CheckTopicVideoAccessServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@Mock
	private AgitMembershipPort agitMembershipPort;

	@InjectMocks
	private CheckTopicVideoAccessService checkTopicVideoAccessService;

	@Test
	void checkAccess_forbiddenWhenVideoHasNoTopicLink() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		given(topicPersistencePort.findAgitUuidsByVideoUuid(videoUuid)).willReturn(List.of());

		assertThat(checkTopicVideoAccessService.checkAccess(videoUuid, userUuid))
				.isEqualTo(TopicVideoAccessStatus.FORBIDDEN);
		verify(agitMembershipPort, never()).findActiveMember(any(), any());
	}

	@Test
	void checkAccess_allowedWhenMemberOfLinkedAgit() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID agitUuid = UUID.randomUUID();
		given(topicPersistencePort.findAgitUuidsByVideoUuid(videoUuid)).willReturn(List.of(agitUuid));
		given(agitMembershipPort.findActiveMember(agitUuid, userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		assertThat(checkTopicVideoAccessService.checkAccess(videoUuid, userUuid))
				.isEqualTo(TopicVideoAccessStatus.ALLOWED);
	}

	@Test
	void checkAccess_forbiddenWhenLinkedButNotMember() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID agitUuid = UUID.randomUUID();
		given(topicPersistencePort.findAgitUuidsByVideoUuid(videoUuid)).willReturn(List.of(agitUuid));
		given(agitMembershipPort.findActiveMember(agitUuid, userUuid)).willReturn(Optional.empty());

		assertThat(checkTopicVideoAccessService.checkAccess(videoUuid, userUuid))
				.isEqualTo(TopicVideoAccessStatus.FORBIDDEN);
	}

	@Test
	void checkAccess_allowedWhenMemberOfAnyLinkedAgit() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		UUID otherAgitUuid = UUID.randomUUID();
		UUID memberAgitUuid = UUID.randomUUID();
		given(topicPersistencePort.findAgitUuidsByVideoUuid(videoUuid))
				.willReturn(List.of(otherAgitUuid, memberAgitUuid));
		given(agitMembershipPort.findActiveMember(otherAgitUuid, userUuid)).willReturn(Optional.empty());
		given(agitMembershipPort.findActiveMember(memberAgitUuid, userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.HOST)));

		assertThat(checkTopicVideoAccessService.checkAccess(videoUuid, userUuid))
				.isEqualTo(TopicVideoAccessStatus.ALLOWED);
	}

	@Test
	void checkAccess_forbiddenWhenUuidMissing() {
		assertThat(checkTopicVideoAccessService.checkAccess(null, UUID.randomUUID()))
				.isEqualTo(TopicVideoAccessStatus.FORBIDDEN);
		assertThat(checkTopicVideoAccessService.checkAccess(UUID.randomUUID(), null))
				.isEqualTo(TopicVideoAccessStatus.FORBIDDEN);
	}
}
