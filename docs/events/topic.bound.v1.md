# topic.bound v1

토픽이 아지트에 묶일 때 발행한다. agit는 방 읽기모델 `topics[]`를 upsert하고, chat은 시스템 메시지를 만든다.

원본 소비자 스펙은 agit `docs/events/topic.bound.v1.md`를 따른다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `topic.bound` |
| Producer | topic-service |
| Consumer | agit-service, chat-service |
| Message Key | `agitUuid` |
| Value format | JSON (type header 없음) |

## Payload

```json
{
  "agitUuid": "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e",
  "topicId": "0190abcd-1111-7abc-def0-123456789abc",
  "startedAt": "2026-08-18T00:00:00Z",
  "occurredAt": "2026-08-18T17:00:00Z"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| agitUuid | UUID | Y | 토픽이 속한 아지트 |
| topicId | string | Y | 토픽 UUID. agit/chat 계약 필드명 (`topicUuid` 아님) |
| startedAt | datetime (UTC) | N | 토픽 시작 시각. 생성 시에는 startAt을 넣는다 |
| occurredAt | datetime (UTC) | Y | 이벤트 발생 시각 |

## Producer 동작 (topic-service)

`POST /api/v1/topics`가 커밋된 뒤 `topic.started`와 함께 발행한다.

## 소비자 기대

- **agit**: 방 읽기모델에 topicId upsert. 없으면 skip
- **chat**: `{주제}가 설정되었습니다` 시스템 메시지
