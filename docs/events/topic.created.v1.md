# topic.created v1

## 개요

토픽이 생성될 때 발행되는 이벤트.
채팅 서비스 등 다른 서비스가 이 이벤트를 소비하여 시스템 메시지를 생성하거나 알림을 전송한다.

## 토픽(Kafka Topic)

`topic.created`

## 방향

| 역할     | 서비스        |
|----------|--------------|
| Producer | topic        |
| Consumer | chat (예정)  |

## 메시지 키

`topicUuid` (String)

## 페이로드

```json
{
  "topicUuid": "UUID",
  "agitUuid": "UUID",
  "creatorUuid": "UUID",
  "title": "토픽 제목",
  "startAt": "2026-08-18T00:00:00",
  "occurredAt": "2026-08-18T17:00:00"
}
```

| 필드          | 타입           | 설명                    |
|--------------|---------------|------------------------|
| topicUuid    | UUID (string) | 생성된 토픽 식별자        |
| agitUuid     | UUID (string) | 토픽이 속한 아지트 식별자  |
| creatorUuid  | UUID (string) | 토픽 생성자 식별자        |
| title        | string        | 토픽 제목               |
| startAt      | datetime      | 토픽 시작 일시           |
| occurredAt   | datetime      | 이벤트 발생 시각          |

## 발행 시점

`POST /api/v1/topics` 요청으로 토픽이 DB에 저장된 후, 트랜잭션 커밋 이후 비동기 발행.

## 소비자 기대 동작

- **채팅 서비스**: 해당 아지트 채팅방에 "새 토픽 '{title}'이 시작되었습니다" 시스템 메시지 생성
- **알림 서비스**: 아지트 멤버에게 푸시 알림 전송 (선택)
