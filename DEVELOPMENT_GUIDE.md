# PLIP 백엔드 서비스 개발 가이드

이 문서는 PLIP 마이크로서비스 개발 시 팀원이 따라야 하는 공통 아키텍처·개발 규칙입니다.

- 템플릿 복사 **직후** 이름·포트·env 설정 → [START.md](START.md)
- **AI 에이전트**용 코드 규칙 → [AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md)
  - 필요시 수정 및 추가하거나 사용하는 Agent IDE에 workspace rules로 추가해 사용하세요

Eureka·API Gateway **연동 절차**는 이 가이드 범위 밖입니다. (초기 YAML on/off 안내는 START.md만 참고)

---

## 1. 참고용 샘플 코드 (`src/test/java/com/sample`)

- `src/test/java/com/sample/**` 아래 코드(예: `auth_service`, `chatting`)는 **구조 참고용**이며, 실제 서비스에 **포함되지 않습니다**.
- `build.gradle`의 `sourceSets`가 `**/com/sample/**`를 컴파일 및 `./gradlew test` 대상에서 제외합니다.
- **레이어·패키지 배치만** 참고하고, 실제 코드는 `src/main/java/com/plip/{service}` 아래에 작성하세요.
- 샘플의 명명(`adaptor`, `vo`, `mysql`/`mongodb`)은 **레거시**입니다. 2절 팀 표준을 따르고, 샘플 철자나 `com.unionclass.`* import는 **복사하지 마세요**.



### 샘플 vs 팀 표준


| 구분               | 팀 표준 (신규 코드)              | 샘플 (`com.sample`)                       |
| ---------------- | ------------------------- | --------------------------------------- |
| 어댑터 폴더           | `adapter`                 | `adaptor`                               |
| 웹 모델             | `adapter.in.web.dto`      | `adaptor.in.web.vo`                     |
| 영속 패키지           | `adapter.out.persistence` | `adaptor.out.mysql` / `mongodb`         |
| Outbound 어댑터 클래스 | `*PersistenceAdapter`     | `*RepositoryAdapter` 등                  |
| Inbound 포트 / 구현  | `*UseCase` / `*Service`   | 동일                                      |
| 도메인              | `domain.model` (JPA 금지)   | 동일 취지                                   |
| 루트 패키지           | `com.plip.{service}`      | `com.sample.*` (오래된 import가 남아 있을 수 있음) |


---



## 2. 아키텍처 표준 (헥사고날)

의존성 방향은 항상 바깥 → 안쪽입니다. (`adapter` → `application` → `domain`)

```text
src/main/java/com/plip/{service}/
├── adapter/
│   ├── in/web/               # Controller, Request/Response DTO
│   └── out/persistence/      # PersistenceAdapter, JPA Entity, Spring Data Repository, mapper
├── application/
│   ├── port/in/              # UseCase 인터페이스 (+ 필요 시 port DTO)
│   ├── port/out/             # Outbound 포트
│   └── service/              # UseCase 구현체
├── domain/
│   └── model/                # 순수 도메인 (JPA / Spring 어노테이션 금지)
└── global/config/            # SwaggerConfig 등 공통 설정
```

규칙:

1. `domain.model`은 JPA·Spring·인프라 라이브러리를 import 하지 않습니다.
2. JPA Entity는 `adapter` 밖으로 노출하지 않습니다. `application`으로 넘기기 전에 Domain으로 매핑합니다.
3. 패키지명에 하이픈(`-`)을 넣지 않습니다. `com.plip.{service}` 형태를 사용합니다. (예시. `com.plip.agit`)
4. 네이밍: `*UseCase`, `*Service`, `*Port` / `*PersistencePort`, `*PersistenceAdapter`.

---



## 3. API 및 문서화



### 3.1 REST (Springdoc OpenAPI)

- Springdoc **코드 퍼스트**: Controller·DTO에 `@Tag`, `@Operation`, `@Schema`를 붙입니다.
- `./gradlew test` 시 `OpenApiGeneratorTest`가 CI에서 함께 실행됩니다 (H2 `test` 프로필).
- **API 명세 조회**: [Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html) (서비스별 통합). repo `docs/openapi.yaml` 커밋은 **필수 아님** ([GIT_CONVENTION.md](GIT_CONVENTION.md)).



### 3.2 Kafka / EDA (필요 시)

- 이벤트 스펙은 `docs/events/{event-name}.v1.md`에 Markdown으로 작성합니다.

---



## 4. DB 및 엔티티 표준

마이크로서비스마다 **독립 DB**(`plip_{servicename}`)만 사용합니다. 타 서비스 DB 직접 접속·JOIN은 금지합니다.

1. 테이블·컬럼명은 소문자 `snake_case`를 사용합니다.
2. PK는 `id` (`BIGINT`, Auto-Increment)입니다. 타 서비스 참조는 `{service}_id` (예: `user_id`, `agit_id`).
3. `created_at`, `updated_at` 등 공통 시각 컬럼은 작업자가 BaseEntity 등으로 직접 작성·상속합니다.
4. `@ManyToOne`, `@OneToOne`은 반드시 `FetchType.LAZY`로 설정합니다.
5. 테이블 추가·변경 시 DDL을 `docs/sql/schema.sql`에 반영·커밋합니다.
6. Entity에는 Lombok `@Data`를 쓰지 않습니다. `@Getter` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 사용합니다.

Security, Redis, Kafka 등 추가 라이브러리는 **기능에 필요할 때만** `build.gradle`에 넣습니다. → START.md 6단계 참고