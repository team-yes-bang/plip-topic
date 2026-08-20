# topic_viewer v1

단건 뷰어 읽기 스냅샷. MySQL이 원본이고, 이 컬렉션은 `GET /topics/{topicUuid}` + `GET /topics/{topicUuid}/videos`를 한 문서로 복제한 것이다. 목록·캘린더는 Redis를 유지한다.

## Collection

| 항목 | 값 |
| --- | --- |
| Collection | `topic_viewer` |
| `_id` | `topicUuid` 문자열 |
| 인덱스 | `_id` unique, `agitUuid` |

## JSON

```json
{
  "_id": "0190abcd-1111-7abc-def0-123456789abc",
  "topicUuid": "0190abcd-1111-7abc-def0-123456789abc",
  "agitUuid": "018f3f6e-8e2a-7b3c-9d4e-5f6a7b8c9d0e",
  "creatorUuid": "01912345-6789-7abc-def0-123456789abe",
  "title": "점심 메뉴",
  "startAt": "2026-08-18T00:00:00",
  "videoCount": 2,
  "videos": [
    {
      "videoUuid": "01912345-6789-7abc-def0-123456789abd",
      "userUuid": "01912345-6789-7abc-def0-123456789abe",
      "createdAt": "2026-08-18T13:45:00"
    }
  ],
  "createdAt": "2026-08-18T09:00:00",
  "deleted": false,
  "projectedAt": "2026-08-20T04:00:00Z"
}
```

| 필드 | 설명 |
| --- | --- |
| topicUuid, agitUuid, creatorUuid | 논리 참조. UUID 문자열 |
| title, startAt, createdAt | 주제 메타 |
| videoCount | `videos` 길이. 붙은 영상 수 |
| videos[] | 격자용. videoUuid / userUuid / createdAt만 |
| deleted | true면 조회에서 제외하고 문서는 삭제한다 |
| projectedAt | MySQL에서 투영한 시각 |

넣지 않는 것: playbackUrl, thumbnailUrl, nickname, agit members, diary 영상.

## 갱신

커밋 성공 후 `TopicViewerSnapshotPort.save` / `delete`. 조회 미스 시 MySQL에서 읽어 다시 save.
