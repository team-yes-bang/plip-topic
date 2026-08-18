# Event specifications

Kafka / EDA 스펙은 `docs/events/{event-name}.v1.md`에 둔다.

| 이벤트 | 이 서비스 역할 | 상대 |
| --- | --- | --- |
| [topic.video.attached.v1](topic.video.attached.v1.md) | Producer | diary |
| [diary.video.bound.v1](diary.video.bound.v1.md) | Consumer | diary |
| [video.uploaded.v1](video.uploaded.v1.md) | Consumer | video |
| [agit.renamed.v1](agit-renamed.v1.md) | Consumer (스냅샷) | agit |
