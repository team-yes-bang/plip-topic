# video.uploaded v1

video-service가 destination `kind=TOPIC` 커밋 후 발행한다. topic-service는 `topicUuid`로 `topic_video`에 붙인다.

다이어리 바인딩은 별도 이벤트 `diary.video.uploaded`다. 원본 produce 스펙은 plip-video `docs/events/video.uploaded.v1.md`.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `video.uploaded` |
| Producer | video-service |
| Consumer | topic-service |
| Consumer Group (topic) | `topic-service` |
| Message Key | `videoUuid` |
| Value format | JSON (type header 없음) |

## Payload (topic이 보는 필드)

```json
{
  "topicUuid": "0190abcd-1111-7abc-def0-123456789abc",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "caption": "캡션",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | topic-service 사용 |
| --- | --- | --- | --- |
| topicUuid | UUID | Y | Y — 없으면 skip |
| videoUuid | UUID | Y | Y |
| userUuid | UUID | Y | Y — video 소유자와 대조 |
| themeUuid | UUID | N | N |
| 기타 | — | N | ignoreUnknown |

## Consumer 동작 (topic-service)

1. `topicUuid` / `videoUuid` / `userUuid` 없으면 skip
2. 토픽이 없으면 skip
3. 아지트 비멤버면 skip. 멤버십 조회 5xx는 throw → Kafka 재시도
4. `GET /internal/videos/{videoUuid}`로 owner 조회. owner ≠ `userUuid`이거나 404면 skip. 5xx/timeout은 throw → Kafka 재시도
5. 이미 붙은 `video_uuid`면 멱등 skip
6. 유저당 토픽당 1개 한도면 skip
7. `topic_video` INSERT. `topic.video.attached`는 발행하지 않는다

video producer는 아지트 멤버십을 보지 않는다. 검증은 topic consumer 책임이다.

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | topic 구독 — 선택 필드 `topicUuid` |
| v1.1 | consumer가 멤버십·video 소유권을 검증. diary는 `diary.video.uploaded`만 구독 |
