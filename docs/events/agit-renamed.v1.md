# agit.renamed.v1

topic 서비스는 아지트 멤버십·방 설정을 소유하지 않는다. 캘린더/목록에 방 제목이 필요하면 이 이벤트로 **스냅샷만** 갱신한다.

## Producer

- agit 서비스

## Consumer

- topic 서비스 (read-model/캐시 보조 필드. 원본 아님)

## Payload

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| agitUuid | UUID | 아지트 식별자 |
| title | string | 변경된 방 제목 |
| occurredAt | datetime | 발생 시각 |

## topic 쪽 규칙

- `agit_uuid`만 topic 테이블에 저장한다.
- 멤버 목록·초대·추방은 저장하지 않는다.
- 제목 스냅샷은 조회 응답 보조용이며, 권한 판단에 쓰지 않는다.
