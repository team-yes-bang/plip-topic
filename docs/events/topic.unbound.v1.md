# topic.unbound v1

토픽이 아지트에서 해제될 때 발행한다. agit는 방 읽기모델 `topics[]`에서 해당 항목을 제거한다.

원본 소비자 스펙은 agit `docs/events/topic.unbound.v1.md`를 따른다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `topic.unbound` |
| Producer | topic-service |
| Consumer | agit-service (chat은 아직 미구독) |
| Message Key | `agitUuid` |
| Value format | JSON (type header 없음) |

## Payload

```json
{
  "agitUuid": "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e",
  "topicId": "0190abcd-1111-7abc-def0-123456789abc",
  "occurredAt": "2026-08-18T17:00:00Z"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| agitUuid | UUID | Y | 토픽이 속한 아지트 |
| topicId | string | Y | 해제한 토픽 UUID. agit 계약 필드명 (`topicUuid` 아님) |
| occurredAt | datetime (UTC) | Y | 이벤트 발생 시각 |

`startedAt`은 unbound 계약에 없다. 생성 시 bound/started와 같은 DTO를 쓰되 null이면 JSON에서 생략한다.

## Producer 동작 (topic-service)

`DELETE /api/v1/topics/{topicUuid}`가 커밋된 뒤 발행한다. 영상이 있어 삭제가 거절되면 발행하지 않는다.

## 소비자 기대

- **agit**: 방 읽기모델이 없으면 skip. `topics[]`에서 해당 `topicId`를 제거. 없어도 no-op
- **chat**: 아직 미구독
