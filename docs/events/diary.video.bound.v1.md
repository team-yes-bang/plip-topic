# diary.video.bound v1

diary-service가 `diary_videos`에 영상을 바인딩한 뒤 발행한다. topic-service는 `topicUuid`가 있으면 같은 `video_uuid`를 `topic_video`에 붙인다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `diary.video.bound` |
| Producer | diary-service |
| Consumer | topic-service |
| Consumer Group | `topic-service` |
| Message Key | `videoUuid` |
| Value format | JSON (type header 없음) |

## Payload

```json
{
  "themeUuid": "01912345-6789-7abc-def0-123456789abc",
  "topicUuid": "0190abcd-1111-7abc-def0-123456789abc",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "occurredAt": "2026-08-18T05:00:00"
}
```

| 필드 | 타입 | 필수 | topic-service 사용 |
| --- | --- | --- | --- |
| themeUuid | UUID | Y | N — 다이어리 테마 |
| topicUuid | UUID | Y* | Y — 붙일 토픽. 없으면 skip |
| videoUuid | UUID | Y | Y — `topic_video.video_uuid` |
| userUuid | UUID | Y | N — 소유권은 다이어리 쪽 검증 |
| occurredAt | datetime | N | N |

> camelCase / snake_case 모두 수용한다. `topicUuid`가 없으면 토픽은 연결하지 않는다(어느 토픽인지 알 수 없음).

## Producer 동작 (diary-service, 구현 대상)

1. 다이어리 바인딩이 커밋된 뒤 발행한다.
2. 사용자가 토픽에도 올리도록 선택한 경우에만 `topicUuid`를 넣는다.
3. `topic.video.attached`를 받아 바인딩한 건은 **재발행하지 않는다**.

## Consumer 동작 (topic-service)

1. `topicUuid`, `videoUuid` 없으면 skip
2. 토픽이 없으면 skip (재시도하지 않음)
3. 동일 `(topic_id, video_uuid)`면 멱등 skip
4. `topic_video` INSERT. `topic.video.attached`는 발행하지 않는다.

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | 최초 정의 — 다이어리 → 토픽 영상 동기화 |
