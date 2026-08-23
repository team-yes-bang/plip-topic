package com.plip.topic.application.port.out;

import java.util.UUID;

public interface AgitMembershipWarmupPort {

	void warmup(UUID agitUuid);
}
