package com.plip.topic.adapter.out.agit;

import com.plip.topic.application.exception.AgitMembershipUnavailableException;
import com.plip.topic.application.port.out.AgitMemberRole;
import com.plip.topic.application.port.out.AgitMembership;
import com.plip.topic.application.port.out.AgitMembershipPort;
import com.plip.topic.application.port.out.AgitMembershipWarmupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisAgitMembershipAdapter implements AgitMembershipPort {

	static final String KEY_PREFIX = "agit:";
	static final String KEY_SUFFIX = ":members";

	private final StringRedisTemplate stringRedisTemplate;
	private final AgitMembershipWarmupPort agitMembershipWarmupPort;

	@Override
	public Optional<AgitMembership> findActiveMember(UUID agitUuid, UUID userUuid) {
		if (agitUuid == null || userUuid == null) {
			return Optional.empty();
		}
		String redisKey = key(agitUuid);
		try {
			if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
				agitMembershipWarmupPort.warmup(agitUuid);
				if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
					throw new AgitMembershipUnavailableException();
				}
			}
			Object value = stringRedisTemplate.opsForHash().get(redisKey, userUuid.toString());
			return toMembership(value);
		} catch (AgitMembershipUnavailableException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new AgitMembershipUnavailableException(exception);
		}
	}

	private Optional<AgitMembership> toMembership(Object value) {
		if (value == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(new AgitMembership(AgitMemberRole.valueOf(value.toString().trim())));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	static String key(UUID agitUuid) {
		return KEY_PREFIX + agitUuid + KEY_SUFFIX;
	}
}
