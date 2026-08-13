package com.unionclass.chatting.adaptor.out.mongodb.reactiveRepository;

import com.mongodb.client.model.changestream.OperationType;
import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.unionclass.chatting.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageGetDto;
import com.unionclass.chatting.application.port.out.ChatServiceReactiveRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class ChatReactiveRepositoryImpl implements ChatServiceReactiveRepositoryPort {

    private final ChatReactiveMongoRepository chatReactiveMongoRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatEntityMapper chatEntityMapper;

    @Override
    public Flux<ChatMessageGetDto> getChatByChatRoomUuid(String chatRoomUuid) {
        return chatEntityMapper.chatMessageGetDtoFlux(chatReactiveMongoRepository.findByChatRoomUuid(chatRoomUuid));
    }

    @Override
    public Flux<ChatMessageGetDto> getLatestChatByChatRoomUuid(String chatRoomUuid) {
        ChangeStreamOptions options = ChangeStreamOptions.builder()
                .filter(Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("operationType").is(OperationType.INSERT.getValue())),
                        Aggregation.match(Criteria.where("fullDocument.chatRoomUuid").is(chatRoomUuid))
                )).build();
        return chatEntityMapper.chatMessageGetDtoFlux(reactiveMongoTemplate.changeStream("chat_message_entity", options, Document.class)
                .map(ChangeStreamEvent::getBody)
                .map(document -> ChatMessageEntity.builder()
                        .id(document.get("_id", ObjectId.class).toString())
                        .chatRoomUuid(document.getString("chatRoomUuid"))
                        .senderUuid(document.getString("senderUuid"))
                        .message(document.getString("message"))
                        .messageType(document.getString("message_type"))
                        .createdAt(document.getDate("createdAt").toInstant())
                        .updatedAt(document.getDate("updatedAt").toInstant())
                        .build()));
    }

}
