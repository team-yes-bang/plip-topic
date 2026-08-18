# video.uploaded v1

video-service가 업로드 완료 후 발행한다. diary-service는 `themeUuid`로 `diary_videos`에 바인딩하고, topic-service는 `topicUuid`가 있으면 `topic_video`에 붙인다.

원본 스펙은 plip-diary `docs/events/video.uploaded.v1.md`를 따른다. 여기 문서는 **topic 구독 규칙**만 적는다.

## Topic

| 항목 | 값 |
| --- | --- |
| Topic | `video.uploaded` |
| Producer | video-service |
| Consumer | diary-service, topic-service |
| Consumer Group (topic) | `topic-service` |
| Message Key | `videoUuid` |
| Value format | JSON (type header 없음) |

## Payload (topic이 보는 필드)

```json
{
  "themeUuid": "01912345-6789-7abc-def0-123456789abc",
  "topicUuid": "0190abcd-1111-7abc-def0-123456789abc",
  "videoUuid": "01912345-6789-7abc-def0-123456789abd",
  "userUuid": "01912345-6789-7abc-def0-123456789abe",
  "caption": "캡션",
  "thumbnailUrl": "https://cdn.example/thumb.jpg",
  "occurredAt": "2026-08-12T11:00:00"
}
```

| 필드 | 타입 | 필수 | topic-service 사용 |
| --- | --- | --- | --- |
| themeUuid | UUID | Y (diary) | N |
| topicUuid | UUID | Y* | Y — 없으면 skip |
| videoUuid | UUID | Y | Y |
| userUuid | UUID | Y | N |
| 기타 | — | N | ignoreUnknown |

토픽과 다이어리에 동시에 올리려면 video-service가 `themeUuid`와 `topicUuid`를 **함께** 넣는다. 두 서비스가 같은 이벤트를 각자 소비하므로 서로 재발행하지 않는다.

## Consumer 동작 (topic-service)

1. `topicUuid` 또는 `videoUuid` 없으면 skip
2. 토픽이 없으면 skip
3. 이미 붙은 `video_uuid`면 멱등 skip
4. `topic_video` INSERT. `topic.video.attached`는 발행하지 않는다

## 버전 이력

| 버전 | 변경 |
| --- | --- |
| v1 | topic 구독 — 선택 필드 `topicUuid` |
