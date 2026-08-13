package com.unionclass.chatting.adaptor.out.mongodb.config;

import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatRoomEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomDataInitializer implements ApplicationRunner {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final List<SeedRoom> DEFAULT_ROOMS = List.of(
            new SeedRoom("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", "일반 채팅방"),
            new SeedRoom("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", "프로젝트 채팅방"),
            new SeedRoom("cccccccc-cccc-4ccc-8ccc-cccccccccccc", "문의 채팅방")
    );

    @Override
    public void run(ApplicationArguments args) {
        Flux.fromIterable(DEFAULT_ROOMS)
                .flatMap(this::upsertIfAbsent)
                .then()
                .doOnSuccess(unused -> log.info("Chat room seed completed"))
                .doOnError(error -> log.warn("Chat room seed failed: {}", error.getMessage()))
                .subscribe();
    }

    private Mono<ChatRoomEntity> upsertIfAbsent(SeedRoom seedRoom) {
        Query query = Query.query(Criteria.where("chatRoomUuid").is(seedRoom.chatRoomUuid()));
        Instant now = Instant.now();

        return reactiveMongoTemplate.exists(query, ChatRoomEntity.class)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.empty();
                    }
                    return reactiveMongoTemplate.save(ChatRoomEntity.builder()
                            .chatRoomUuid(seedRoom.chatRoomUuid())
                            .roomName(seedRoom.roomName())
                            .participantEntityList(Collections.emptyList())
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                });
    }

    private record SeedRoom(String chatRoomUuid, String roomName) {
    }
}
