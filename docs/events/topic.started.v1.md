# topic.started v1

토픽이 시작될 때 발행한다. agit는 `topics[].startedAt`을 갱신하고, chat은 시스템 메시지를 만든다.

원본 소비자 스펙은 agit `docs/events/topic.started.v1.md`를 따른다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `topic.started` |
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
| agitUuid | UUID | Y | 아지트 UUID |
| topicId | string | Y | 토픽 UUID |
| startedAt | datetime (UTC) | Y | 토픽 시작 시각 |
| occurredAt | datetime (UTC) | Y | 이벤트 발생 시각 |

## Producer 동작 (topic-service)

`POST /api/v1/topics`가 커밋된 뒤 `topic.bound`와 함께 발행한다. 생성 시 startAt(없으면 오늘 00:00)을 `startedAt`으로 넣는다.

## 소비자 기대

- **agit**: `startedAt`이 없으면 skip. 읽기모델에 topicId + startedAt 반영
- **chat**: `주제가 시작되었습니다` 시스템 메시지
