package com.plip.topic.adapter.out.video;

import java.util.UUID;

record InternalVideoOwnershipResponse(UUID videoUuid, UUID userUuid) {
}
