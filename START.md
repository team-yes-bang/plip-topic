# 서비스 템플릿 설정 및 초기화 가이드 (Quick Start Guide)

`plip-template`을 복사한 뒤 **서비스 repo 초기 설정**을 다룹니다.  
Develop PC 자동 배포(CI/CD) **흐름·인프라 등록**은 [plip-develop-env/DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md)를 참고하세요.

| 문서 | 역할 |
| --- | --- |
| **START.md** (본 문서) | 템플릿 적용 · YML/env · **서비스 repo** CI/CD 준비 |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | 팀 개발 규칙 (헥사고날, DB, Springdoc) |
| [AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md) | AI 에이전트용 코드 생성 제약 |
| [GIT_CONVENTION.md](GIT_CONVENTION.md) | Issue / 브랜치 / 커밋 / PR / CI |
| [DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md) | Develop PC deploy · Gateway 통합 API 문서 (인프라 담당) |

### AI 가이드 문서 사용

[AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md)는 Cursor 등 AI 에이전트가 따를 규칙입니다.  
에이전트·팀 정책에 맞게 **파일명을 바꾸거나** (예: `.cursor/rules/…`, `AGENTS.md`, `copilot-instructions.md`) **필요한 섹션만 복사**해 사용해도 됩니다.  
단, 헥사고날 레이어·명명(`adapter`, `dto`) 등 **코드 규칙의 내용**은 [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)와 맞추세요.

초기 설정 후 일상 개발은 DEVELOPMENT_GUIDE를 따릅니다. `src/test/java/com/sample`은 빌드 제외 참고용입니다.

## Check List

- [ ] 1. 프로젝트 이름 · 패키지 · `OpenApiGeneratorTest` (`com.plip.template` → `com.plip.{service}`)
- [ ] 2. `settings.gradle` · 메인 클래스 · `spring.application.name` (= 서비스 **id**)
- [ ] 3. `application.yaml` / `application-local.yml` / `application-docker.yml` · `.env.example`
- [ ] 4. 로컬 기동 (MySQL compose + IDE bootRun)
- [ ] 5. 서비스 repo CI/CD — workflow `"service"` (**5단계**). env/Gateway는 **인프라 담당**

---

## 샘플 코드 (`com.sample`) — 참고용 (빌드 제외)

`src/test/java/com/sample/**`는 `./gradlew build` / 테스트 대상에서 **제외**됩니다 (`build.gradle` `sourceSets`).  
서비스 코드·테스트는 `com.plip.{service}`에 작성하세요. 신규 코드 명명은 DEVELOPMENT_GUIDE 표준(`adapter`, `dto`)을 따릅니다.

---

## 1단계: 프로젝트 기본 정보

`plip-template`을 복사해 `plip-{service}`를 만들 때 아래를 **서비스 id**에 맞게 바꿉니다.

### 1) `settings.gradle`

```groovy
// plip-template (그대로)
rootProject.name = 'template'

// plip-{service} 로 복사한 경우 — id와 동일한 짧은 이름 (예: user, media)
rootProject.name = '{service}'
```

> `-service` 접미사는 쓰지 않습니다. Gateway `lb://`, manifest `id`와 동일하게 유지합니다.

### 2) `build.gradle`

`group` / `version` / `sourceSets`의 `com/sample` exclude는 그대로 유지합니다.

### 3) 패키지 · 메인 클래스 · 테스트 (IDE Refactor)

| 항목 | plip-template | 복사 후 (예: `{service}`) |
| --- | --- | --- |
| 패키지 | `com.plip.template` | `com.plip.{service}` |
| 메인 클래스 | `TemplateApplication.java` | `{Service}Application.java` |
| OpenAPI 테스트 | `OpenApiGeneratorTest.java` | 동일 클래스명, 패키지 `{service}` |

- `src/main/java/com/plip/template` → Rename (Shift+F6) → `com.plip.{service}`
- `src/test/java/com/plip/template` 동일 (`OpenApiGeneratorTest`, `*ApplicationTests` 포함)

---

## 2단계: Application YML · Env

| 파일 | 역할 |
| --- | --- |
| `application.yaml` | 공통 — `spring.application.name`, datasource env 키, Eureka 기본 |
| `application-local.yml` | 개발 PC IDE/`bootRun` — 포트, `localhost:3308` DB |
| `application-docker.yml` | Develop PC compose — `mysql:3306`, Eureka hostname = **서비스 id** |
| `.env.example` | `DB_*`, `MYSQL_DATABASE`, `SERVER_PORT` 참고 (**.env` Git 금지**) |

> Spring Boot는 `.env`를 자동 로드하지 않습니다. IDE Run Configuration 또는 `export`로 env 주입.

### Naming (서비스 id 하나로 통일)

Gateway `lb://`, manifest `id`, workflow `"service"`, Eureka docker hostname 모두 **동일한 id**를 씁니다.

| 항목 | plip-template | 복사 후 변경 |
| --- | --- | --- |
| `spring.application.name` | `template` | `{service}` |
| `rootProject.name` | `template` | `{service}` |
| MySQL 스키마 | `plip_template` | `plip_{service}` |
| Gateway path | `/template/**` | `/{service}/**` |

### 로컬 포트 (팀 할당)

| 서비스 | `application-local.yml` 기본 포트 |
| --- | --- |
| Gateway (공통) | `8000` |
| template | `8081` |
| (기타) | 팀 할당표 · 인프라 담당자 확인 |

`plip-template` 기본값은 **`8081`**. 충돌 시 `SERVER_PORT` env로 오버라이드.

### `application.yaml` (공통) — template 기준

```yaml
spring:
  application:
    name: template   # 👈 서비스 id (Gateway lb://template 와 동일)
  profiles:
    active: local
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    properties:
      hibernate:
        format_sql: true

eureka:
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 5
    lease-expiration-duration-in-seconds: 10
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
```

복사 시 `spring.application.name`만 `{service}`로 변경.

> **`local`**: `eureka.client.enabled: false` (local yml).  
> **`docker`**: `enabled: true` + hostname = 서비스 id (`application-docker.yml`).

### `application-local.yml` — template 기준

필요 시 이 파일을 `.gitignore`에 넣어 **로컬 전용**으로 쓸 수 있습니다.

```yaml
server:
  port: ${SERVER_PORT:8081}

spring:
  datasource:
    url: jdbc:mysql://localhost:3308/plip_template?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

eureka:
  instance:
    prefer-ip-address: false
    hostname: localhost
  client:
    enabled: false   # Develop PC Docker Eureka 오염 방지. 연동 테스트 시에만 true
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

복사 시: 포트(팀 할당), `plip_template` → `plip_{service}`.

- DB 포트 **`3308`**: Develop PC 공용 MySQL 또는 repo `docker-compose.yml` (MySQL)

### `application-docker.yml` — template 기준

Develop PC `docker-compose.services.yml`과 hostname · DB 스키마가 일치해야 합니다.

```yaml
eureka:
  instance:
    hostname: template   # 👈 서비스 id. compose EUREKA_INSTANCE_HOSTNAME 과 동일
    prefer-ip-address: false
  client:
    enabled: true
```

복사 시: `plip_template` → `plip_{service}`, `hostname: template` → `hostname: {service}`.

### `.env.example` — template 기준

```bash
DB_USERNAME=root
DB_PASSWORD=changeme
SERVER_PORT=8080
MYSQL_PORT=3308
MYSQL_DATABASE=plip_template
```

복사 시 `MYSQL_DATABASE=plip_{service}`.

---

## 3단계: 로컬 실행 (개발용)

통합 API 문서: [Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html). 로컬에서는 **서비스 기동**만 확인합니다.

1. MySQL: `docker compose up -d` (repo `docker-compose.yml` — **MySQL만** 기동)
2. env: `DB_USERNAME`, `DB_PASSWORD`
3. `{Service}Application` 실행 — profile: `local`
4. 예: `http://localhost:8081/api/test` 응답 확인

---

## 4단계: 의존성 추가 (필요 시)

Security, Redis 등은 **사용할 때** `build.gradle` `dependencies`에 추가.  
Git hooks: `./gradlew compileJava` 등 1회 실행 후 [GIT_CONVENTION.md](GIT_CONVENTION.md) 참고.

---

## 5단계: Develop PC CI/CD — 서비스 repo에서 할 일

`develop` push 시 test → Develop PC deploy가 동작하려면 **아래는 서비스 repo 담당자**가 `plip-template` 기준으로 맞춥니다.

> **`plip-develop-env` 등록**, **Gateway route**, **org secret**, **runner**는 **인프라 담당** — 서비스 담당자가 직접 수정하지 않습니다.  
> 흐름 · 검증: [DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md)

### 서비스 repo 체크리스트

| 파일 | plip-template | 신규 서비스 (`plip-{service}`) |
| --- | --- | --- |
| `Dockerfile` | repo 루트 | 그대로 사용 |
| `application-docker.yml` | `hostname: template`, `plip_template` | `{service}`, `plip_{service}` |
| `.github/workflows/test.yml` | 포함 | 그대로 사용 |
| `.github/workflows/deploy-develop-pc.yml` | `"service": "template"` | **`"service": "{service}"`만 변경** |

```yaml
# deploy-develop-pc.yml — 변경 예 (template → media)
"service": "media"
```

### 서비스 담당자 마무리

- [ ] 1~4단계 완료
- [ ] `deploy-develop-pc.yml`의 `"service"` = manifest **id**와 동일
- [ ] `develop` push → 본 repo Actions `Deploy to Develop PC` 성공
- [ ] Develop PC·Gateway 확인은 인프라 등록 후 [DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md)

---

## FAQ

- **Q. `template-service`처럼 긴 이름을 써도 되나요?**  
  아니요. 짧은 id (`template`, `user`, …) 하나로 Gateway · Eureka · manifest · workflow를 맞춥니다.

- **Q. API 명세(Swagger)는 어디서 보나요?**  
  [Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html) (서비스별 통합). repo `docs/openapi.yaml` 커밋은 필수가 아닙니다.

- **Q. `Could not resolve placeholder 'DB_PASSWORD'`**  
  `DB_USERNAME`, `DB_PASSWORD` env 미설정. IDE 또는 터미널 export 후 재실행.

- **Q. 로컬 DB 포트가 3306이 아닌 3308인 이유**  
  repo `docker-compose.yml` 및 Develop PC 공용 MySQL이 호스트 **3308**에 노출합니다.

- **Q. develop push 후 Develop PC에 안 올라가요**  
  본 repo Actions `trigger-deploy` 확인. Develop PC deploy는 [DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md).

- **Q. AI 가이드는 꼭 `AI_CODING_GUIDELINES.md` 이름이어야 하나요?**  
  아니요. 에이전트에 맞게 파일명·경로를 바꿔도 됩니다. 규칙 **내용**은 DEVELOPMENT_GUIDE와 일치시키세요.
