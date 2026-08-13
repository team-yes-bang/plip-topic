package com.unionclass.chatting.adaptor.out.mongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ParticipantEntity {

    @Id
    private String id;
    private String userUuid;
    private String nickName;
    private Integer unreadCount;

}
