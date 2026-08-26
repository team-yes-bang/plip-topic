package com.plip.topic.application.service;

import com.plip.topic.application.exception.AgitMembershipUnavailableException;
import com.plip.topic.application.exception.ForbiddenActorException;
import com.plip.topic.application.exception.UnauthenticatedActorException;
import com.plip.topic.application.exception.VideoOwnershipUnavailableException;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.TopicPersistencePort;
import com.plip.topic.application.port.out.TopicReadCachePort;
import com.plip.topic.application.port.out.TopicVideoEventPort;
import com.plip.topic.application.port.out.TopicViewerSnapshotPort;
import com.plip.topic.application.port.out.VideoOwnershipPort;
import com.plip.topic.domain.model.Topic;
import com.plip.topic.domain.model.TopicVideoLimitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TopicVideoServiceTest {

	@Mock
	private TopicPersistencePort topicPersistencePort;

	@Mock
	private TopicReadCachePort topicReadCachePort;

	@Mock
	private TopicViewerSnapshotPort topicViewerSnapshotPort;

	@Mock
	private TopicVideoEventPort topicVideoEventPort;

	@Mock
	private AgitMembershipPort agitMembershipPort;

	@Mock
	private VideoOwnershipPort videoOwnershipPort;

	@InjectMocks
	private TopicVideoService topicVideoService;

	@Test
	void tryAttach_delegatesToPersistenceAndEvictsCache() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"제목",
				LocalDateTime.of(2026, 8, 18, 0, 0)
		);
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(userUuid));
		given(topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid)).willReturn(true);

		assertThat(topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid)).isTrue();
		verify(topicPersistencePort).addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid);
		verify(topicReadCachePort).evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		verify(topicViewerSnapshotPort).save(topic);
		verify(topicVideoEventPort, never()).publishAttached(any(), any(), any(), any());
	}

	@Test
	void tryAttach_swallowsUserLimit() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(userUuid));
		given(topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid))
				.willThrow(new TopicVideoLimitException());

		assertThat(topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid)).isFalse();
		verify(topicReadCachePort, never()).evict(any(), any());
		verify(topicViewerSnapshotPort, never()).save(any(Topic.class));
	}

	@Test
	void tryAttach_skipsMissingTopic() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThat(topicVideoService.tryAttach(topicUuid, UUID.randomUUID(), UUID.randomUUID())).isFalse();
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void tryAttach_skipsNonMember() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid)).willReturn(Optional.empty());

		assertThat(topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid)).isFalse();
		verify(videoOwnershipPort, never()).findOwnerUuid(any());
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void tryAttach_skipsWhenOwnerMismatch() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(UUID.randomUUID()));

		assertThat(topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid)).isFalse();
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void tryAttach_skipsWhenVideoMissing() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.empty());

		assertThat(topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid)).isFalse();
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void tryAttach_throwsWhenOwnershipUnavailable() {
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willThrow(new VideoOwnershipUnavailableException());

		assertThatThrownBy(() -> topicVideoService.tryAttach(topic.getTopicUuid(), videoUuid, userUuid))
				.isInstanceOf(VideoOwnershipUnavailableException.class);
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void tryAttach_throwsWhenMembershipUnavailable() {
		UUID userUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willThrow(new AgitMembershipUnavailableException());

		assertThatThrownBy(() -> topicVideoService.tryAttach(topic.getTopicUuid(), UUID.randomUUID(), userUuid))
				.isInstanceOf(AgitMembershipUnavailableException.class);
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void attachOrThrow_requiresExistingTopic() {
		UUID topicUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topicUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicVideoService.attachOrThrow(
				topicUuid, UUID.randomUUID(), UUID.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("토픽이 존재하지 않습니다.");
	}

	@Test
	void attachOrThrow_publishesAttachedOnNewVideo() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		Topic withVideo = topic.attachVideo(userUuid, videoUuid);
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid()))
				.willReturn(Optional.of(topic), Optional.of(withVideo));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(userUuid));
		given(topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid)).willReturn(true);

		assertThat(topicVideoService.attachOrThrow(topic.getTopicUuid(), videoUuid, userUuid)).isTrue();
		verify(topicVideoEventPort).publishAttached(topic.getTopicUuid(), topic.getAgitUuid(), videoUuid, userUuid);
	}

	@Test
	void attachOrThrow_doesNotPublishWhenSameVideo() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(userUuid));
		given(topicPersistencePort.addVideoIfAbsent(topic.getTopicUuid(), videoUuid, userUuid)).willReturn(false);

		assertThat(topicVideoService.attachOrThrow(topic.getTopicUuid(), videoUuid, userUuid)).isFalse();
		verify(topicVideoEventPort, never()).publishAttached(any(), any(), any(), any());
	}

	@Test
	void attachOrThrow_forbidsNonMember() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		UUID actorUuid = UUID.randomUUID();
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), actorUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicVideoService.attachOrThrow(
				topic.getTopicUuid(), UUID.randomUUID(), actorUuid))
				.isInstanceOf(ForbiddenActorException.class);
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void attachOrThrow_forbidsNonOwner() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID videoUuid = UUID.randomUUID();
		UUID actorUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), actorUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));
		given(videoOwnershipPort.findOwnerUuid(videoUuid)).willReturn(Optional.of(UUID.randomUUID()));

		assertThatThrownBy(() -> topicVideoService.attachOrThrow(topic.getTopicUuid(), videoUuid, actorUuid))
				.isInstanceOf(ForbiddenActorException.class);
		verify(topicPersistencePort, never()).addVideoIfAbsent(any(), any(), any());
	}

	@Test
	void attachOrThrow_requiresActor() {
		assertThatThrownBy(() -> topicVideoService.attachOrThrow(
				UUID.randomUUID(), UUID.randomUUID(), null))
				.isInstanceOf(UnauthenticatedActorException.class);
	}

	@Test
	void detach_removesVideoAndEvicts() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID videoUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		topicVideoService.detach(topic.getTopicUuid(), videoUuid, userUuid);

		verify(topicPersistencePort).removeVideo(topic.getTopicUuid(), videoUuid, userUuid);
		verify(topicReadCachePort).evict(topic.getAgitUuid(), topic.getStartAt().toLocalDate());
		verify(topicViewerSnapshotPort).save(topic);
		verify(topicViewerSnapshotPort, never()).delete(any());
	}

	@Test
	void detach_forbidsNonMember() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		UUID userUuid = UUID.randomUUID();
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid)).willReturn(Optional.empty());

		assertThatThrownBy(() -> topicVideoService.detach(topic.getTopicUuid(), UUID.randomUUID(), userUuid))
				.isInstanceOf(ForbiddenActorException.class);
		verify(topicPersistencePort, never()).removeVideo(any(), any(), any());
	}

	@Test
	void list_usesSnapshotWithoutPersistence() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0))
				.attachVideo(userUuid, videoUuid);
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		var results = topicVideoService.list(topic.getTopicUuid(), userUuid);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getVideoUuid()).isEqualTo(videoUuid);
		assertThat(results.get(0).getUserUuid()).isEqualTo(userUuid);
		verify(topicPersistencePort, never()).findByTopicUuid(any());
	}

	@Test
	void list_loadsPersistenceWhenSnapshotHasNoVideos() {
		UUID userUuid = UUID.randomUUID();
		UUID videoUuid = UUID.randomUUID();
		Topic emptySnapshot = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		Topic persisted = emptySnapshot.attachVideo(userUuid, videoUuid);
		given(topicViewerSnapshotPort.findByTopicUuid(emptySnapshot.getTopicUuid())).willReturn(Optional.of(emptySnapshot));
		given(topicPersistencePort.findByTopicUuid(emptySnapshot.getTopicUuid())).willReturn(Optional.of(persisted));
		given(agitMembershipPort.findActiveMember(emptySnapshot.getAgitUuid(), userUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		var results = topicVideoService.list(emptySnapshot.getTopicUuid(), userUuid);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getVideoUuid()).isEqualTo(videoUuid);
		verify(topicPersistencePort).findByTopicUuid(emptySnapshot.getTopicUuid());
		verify(topicViewerSnapshotPort).save(persisted);
	}

	@Test
	void list_loadsPersistenceOnSnapshotMiss() {
		Topic topic = Topic.create(UUID.randomUUID(), UUID.randomUUID(), "제목", LocalDateTime.of(2026, 8, 18, 0, 0));
		given(topicViewerSnapshotPort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.empty());
		given(topicPersistencePort.findByTopicUuid(topic.getTopicUuid())).willReturn(Optional.of(topic));
		UUID actorUuid = UUID.randomUUID();
		given(agitMembershipPort.findActiveMember(topic.getAgitUuid(), actorUuid))
				.willReturn(Optional.of(new AgitMembership(AgitMemberRole.GUEST)));

		var results = topicVideoService.list(topic.getTopicUuid(), actorUuid);

		assertThat(results).isEmpty();
		verify(topicViewerSnapshotPort).save(topic);
	}
}
