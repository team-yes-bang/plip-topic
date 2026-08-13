package com.unionclass.chatting.adaptor.out.mongodb.restRepository;

import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatMessageEntity;
import com.unionclass.chatting.adaptor.out.mongodb.entity.ChatRoomEntity;
import com.unionclass.chatting.adaptor.out.mongodb.mapper.ChatEntityMapper;
import com.unionclass.chatting.application.port.dto.ChatMessageSaveDto;
import com.unionclass.chatting.application.port.dto.ChatRoomGetDto;
import com.unionclass.chatting.application.port.dto.ChatRoomSaveDto;
import com.unionclass.chatting.application.port.out.ChatServiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Repository
@RequiredArgsConstructor
public class ChaRestRepositoryImpl implements ChatServiceRepositoryPort {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ChatEntityMapper chatEntityMapper;

    private static final Comparator<ChatRoomGetDto> LATEST_MESSAGE_FIRST =
            Comparator.comparing(
                    ChatRoomGetDto::getLastMessageAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );

    @Override
    public Mono<Void> sendChatMessage(ChatMessageSaveDto chatMessageSaveDto) {
        return reactiveMongoTemplate.save(chatEntityMapper.toEntity(chatMessageSaveDto)).then();
    }

    @Override
    public Flux<ChatRoomGetDto> getChatRooms() {
        return reactiveMongoTemplate.findAll(ChatRoomEntity.class)
                .flatMap(this::toChatRoomGetDtoWithLastMessage)
                .sort(LATEST_MESSAGE_FIRST);
    }

    @Override
    public Mono<ChatRoomGetDto> getChatRoomByUuid(String chatRoomUuid) {
        Query query = Query.query(Criteria.where("chatRoomUuid").is(chatRoomUuid));
        return reactiveMongoTemplate.findOne(query, ChatRoomEntity.class)
                .flatMap(this::toChatRoomGetDtoWithLastMessage);
    }

    @Override
    public Mono<ChatRoomGetDto> saveChatRoom(ChatRoomSaveDto chatRoomSaveDto) {
        ChatRoomEntity entity = chatEntityMapper.toChatRoomEntity(chatRoomSaveDto);
        return reactiveMongoTemplate.save(entity)
                .map(chatEntityMapper::toChatRoomGetDto);
    }

    private Mono<ChatRoomGetDto> toChatRoomGetDtoWithLastMessage(ChatRoomEntity entity) {
        Query lastMessageQuery = Query.query(Criteria.where("chatRoomUuid").is(entity.getChatRoomUuid()))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(1);

        return reactiveMongoTemplate.findOne(lastMessageQuery, ChatMessageEntity.class)
                .map(lastMessage -> chatEntityMapper.toChatRoomGetDto(entity, lastMessage))
                .defaultIfEmpty(chatEntityMapper.toChatRoomGetDto(entity));
    }
}
