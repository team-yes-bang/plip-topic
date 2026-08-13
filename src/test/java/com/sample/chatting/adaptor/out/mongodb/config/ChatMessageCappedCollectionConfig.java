package com.unionclass.chatting.adaptor.out.mongodb.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;

/**
 * {@code @Tailable} cursors require a capped collection.
 * Ensures {@code chat_message_entity} is capped at startup (local/dev friendly).
 * <p>
 * WARNING: If the collection already exists as non-capped, it is dropped and recreated —
 * all existing documents in {@code chat_message_entity} are lost.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatMessageCappedCollectionConfig {

    private static final String COLLECTION = "chat_message_entity";
    private static final long CAPPED_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Bean
    ApplicationRunner ensureChatMessageCappedCollection() {
        return args -> reactiveMongoTemplate.collectionExists(COLLECTION)
                .flatMap(exists -> {
                    if (!exists) {
                        log.info("Creating capped collection '{}' (size={} bytes)", COLLECTION, CAPPED_SIZE_BYTES);
                        return createCappedCollection();
                    }
                    return isCapped().flatMap(capped -> {
                        if (capped) {
                            log.debug("Collection '{}' is already capped; skipping", COLLECTION);
                            return Mono.empty();
                        }
                        log.warn(
                                "Collection '{}' exists but is NOT capped. Dropping and recreating as capped "
                                        + "(size={} bytes). DATA LOSS: all documents in this collection will be deleted.",
                                COLLECTION,
                                CAPPED_SIZE_BYTES
                        );
                        return reactiveMongoTemplate.dropCollection(COLLECTION)
                                .then(createCappedCollection());
                    });
                })
                .block();
    }

    private Mono<Boolean> isCapped() {
        Document filter = new Document("name", COLLECTION);
        return reactiveMongoTemplate.execute(db ->
                        Mono.from(db.listCollections().filter(filter).first()))
                .next()
                .map(info -> {
                    Document options = info.get("options", Document.class);
                    return options != null && Boolean.TRUE.equals(options.getBoolean("capped"));
                })
                .defaultIfEmpty(false);
    }

    private Mono<Void> createCappedCollection() {
        return reactiveMongoTemplate.createCollection(
                        COLLECTION,
                        CollectionOptions.empty().capped().size(CAPPED_SIZE_BYTES))
                .then();
    }
}
