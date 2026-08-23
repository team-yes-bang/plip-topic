package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.exception.AgitMembershipUnavailableException;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembershipWarmupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisAgitMembershipAdapterTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private AgitMembershipWarmupPort agitMembershipWarmupPort;

	@Mock
	@SuppressWarnings("rawtypes")
	private HashOperations hashOperations;

	private RedisAgitMembershipAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new RedisAgitMembershipAdapter(stringRedisTemplate, agitMembershipWarmupPort);
	}

	@SuppressWarnings("unchecked")
	private void stubHashGet(String key, String field, Object value) {
		given(stringRedisTemplate.opsForHash()).willReturn(hashOperations);
		given(hashOperations.get(key, field)).willReturn(value);
	}

	@Test
	void findActiveMember_returnsRoleWhenHashHit() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String key = RedisAgitMembershipAdapter.key(agitUuid);
		given(stringRedisTemplate.hasKey(key)).willReturn(true);
		stubHashGet(key, userUuid.toString(), "GUEST");


		assertThat(adapter.findActiveMember(agitUuid, userUuid))
				.hasValueSatisfying(membership -> assertThat(membership.role()).isEqualTo(AgitMemberRole.GUEST));
		verify(agitMembershipWarmupPort, never()).warmup(agitUuid);
	}

	@Test
	void findActiveMember_returnsEmptyWhenKeyExistsAndFieldMissing() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String key = RedisAgitMembershipAdapter.key(agitUuid);
		given(stringRedisTemplate.hasKey(key)).willReturn(true);
		stubHashGet(key, userUuid.toString(), null);

		assertThat(adapter.findActiveMember(agitUuid, userUuid)).isEmpty();
		verify(agitMembershipWarmupPort, never()).warmup(agitUuid);
	}

	@Test
	void findActiveMember_warmsOnceOnKeyMissThenReturnsRole() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String key = RedisAgitMembershipAdapter.key(agitUuid);
		given(stringRedisTemplate.hasKey(key)).willReturn(false, true);
		stubHashGet(key, userUuid.toString(), "HOST");

		assertThat(adapter.findActiveMember(agitUuid, userUuid))
				.hasValueSatisfying(membership -> assertThat(membership.isHost()).isTrue());
		verify(agitMembershipWarmupPort, times(1)).warmup(agitUuid);
	}

	@Test
	void findActiveMember_throwsWhenWarmupFails() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String key = RedisAgitMembershipAdapter.key(agitUuid);
		given(stringRedisTemplate.hasKey(key)).willReturn(false);
		org.mockito.BDDMockito.willThrow(new AgitMembershipUnavailableException())
				.given(agitMembershipWarmupPort).warmup(agitUuid);

		assertThatThrownBy(() -> adapter.findActiveMember(agitUuid, userUuid))
				.isInstanceOf(AgitMembershipUnavailableException.class);
		verify(stringRedisTemplate, times(1)).hasKey(key);
	}

	@Test
	void findActiveMember_throwsWhenKeyStillMissingAfterWarmup() {
		UUID agitUuid = UUID.randomUUID();
		UUID userUuid = UUID.randomUUID();
		String key = RedisAgitMembershipAdapter.key(agitUuid);
		given(stringRedisTemplate.hasKey(key)).willReturn(false, false);

		assertThatThrownBy(() -> adapter.findActiveMember(agitUuid, userUuid))
				.isInstanceOf(AgitMembershipUnavailableException.class);
		verify(agitMembershipWarmupPort).warmup(agitUuid);
	}
}
