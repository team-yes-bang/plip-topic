# AI Agent Coding Guidelines and Context

> To AI agents (Cursor, Copilot, ChatGPT):
> You are an expert Java 17 / Spring Boot developer specializing in Hexagonal Architecture.
> Follow every rule below when generating or modifying code in this repository.

Human onboarding and local setup live in [START.md](START.md).
Full team rules live in [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md).

---

## 1. Tech stack and reference rules

- **Java:** 17
- **Framework:** Spring Boot 4.0.x
- **Build:** Gradle (Groovy DSL)
- **Architecture:** Hexagonal (Ports and Adapters)
- **Reference only:** `src/test/java/com/sample/**` is excluded from Gradle `sourceSets`. Use it **only** for structural / layering ideas.
- Do **not** write operational code under `com.sample`.
- All active code must live under `com.plip.{service}`.
- Do **not** copy sample spellings (`adaptor`, `vo`) or stale imports (`com.unionclass.*`). Use the team standard below.

---

## 2. Hexagonal rules

1. **Layer isolation**
   - `domain.model`: no JPA (`jakarta.persistence.*`), Spring, or infrastructure imports; pure Java.
   - `adapter.out.persistence`: JPA entities, Spring Data repositories, mappers, `*PersistenceAdapter`.
   - Never return JPA entities to `application` or as REST payloads; map to domain / web DTOs.

2. **Package and naming**

```text
com.plip.{service}/
  adapter/in/web/          # Controller, dto
  adapter/out/persistence/
  application/port/in|out/
  application/service/
  domain/model/
  global/config/
```

| Concept | Standard |
|---------|----------|
| Inbound port | `*UseCase.java` |
| Use case impl | `*Service.java` (`@Service`) |
| Outbound port | `*Port.java` / `*PersistencePort.java` |
| Outbound adapter | `*PersistenceAdapter.java` (`@Component`) |
| Web DTOs | `adapter.in.web.dto` |

Sample code may use `adaptor` / `vo` / `mysql`; **new code must use** `adapter` / `dto` / `persistence`.

---

## 3. Database and entities

1. lowercase `snake_case` for tables and columns.
2. PK column `id` (BIGINT, identity). Cross-service FKs as `{service}_id`.
3. Write entities in `adapter.out.persistence` as needed; do not assume shared entities exist.
4. Track `created_at` / `updated_at` on entities.
5. `@ManyToOne` / `@OneToOne` must use `fetch = FetchType.LAZY`.
6. Never use Lombok `@Data` on entities; use `@Getter` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
7. Update `docs/sql/schema.sql` when schema changes.

---

## 4. API and docs

1. Controllers and DTOs must use Springdoc annotations (`@Tag`, `@Operation`, `@Schema`).
2. Request/response DTOs stay in `adapter.in.web.dto`.
3. Domain models must not be REST response bodies.
4. API spec review: [Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html) (unified). Do not require committing `docs/openapi.yaml`.
5. Event specs (when needed): `docs/events/{event-name}.v1.md`.

---

## 5. Dependencies

Do not invent unused starters. Add Validation, Security, Redis, Kafka, etc. to `build.gradle` only when the feature requires them.
