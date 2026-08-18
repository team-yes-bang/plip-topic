# topic.video.attached v1

topic-service가 `topic_video`에 영상을 붙인 뒤 발행한다. diary-service는 같은 `video_uuid`를 `diary_videos`에 바인딩한다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `topic.video.attached` |
| Producer | topic-service |
| Consumer | diary-service |
| Consumer Group | `diary-service` |
| Message Key | `videoUuid` |
| Value format | JSON (type header 없음) |

## Payload

```json
{
  "topicUuid": "0190abcd-1111-7abc-def0-123456789abc",
  "agitUuid": "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "occurredAt": "2026-08-18T05:00:00"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| topicUuid | UUID | Y | 영상이 붙은 토픽 |
| agitUuid | UUID | Y | 방. 다이어리는 저장하지 않아도 된다 |
| videoUuid | UUID | Y | 미디어 서비스 논리 참조 |
| userUuid | UUID | Y | 업로더(토픽 생성자). 다이어리 테마 소유권 검증 |
| occurredAt | datetime | Y | 발행 시각 |

## Producer 동작 (topic-service)

1. `POST /api/v1/topics`가 `videoUuids`를 포함해 커밋되면 영상마다 1건 발행한다.
2. Kafka inbound(`video.uploaded`, `diary.video.bound`)로 붙인 영상은 **재발행하지 않는다**. 루프 방지.

## Consumer 동작 (diary-service, 구현 대상)

1. `userUuid`의 활성 테마를 찾는다. 기본 테마 `일상`이 있으면 그것을 쓴다.
2. 테마가 없으면 warn + skip.
3. 동일 `(theme_id, video_uuid)` 활성이면 멱등 skip.
4. `diary_videos` INSERT. `topic.video.attached`를 다시 토픽으로 보내지 않는다.

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — 토픽 → 다이어리 영상 동기화 |
