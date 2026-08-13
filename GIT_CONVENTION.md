# Git Convention

이 문서는 [`.github/`](.github/) Issue·PR 템플릿과 CI를 기준으로 한 Git 작업 규칙입니다.

| 경로 | 역할 |
| --- | --- |
| [`.github/ISSUE_TEMPLATE/`](.github/ISSUE_TEMPLATE/) | 이슈 유형·제목·라벨·본문 |
| [`.github/pull_request_template.md`](.github/pull_request_template.md) | PR 제목·본문 양식 |
| [`.github/workflows/test.yml`](.github/workflows/test.yml) | `develop` / `main` PR·push 시 `./gradlew test` |

---

## 1. 기본 흐름

```text
Issue 생성 → 브랜치 생성 → 커밋 → PR → CI(Test) 통과 → 리뷰 → 머지
```

1. 작업 전 Issue를 만들고 번호를 확보합니다.
2. PR 제목·본문에 동일 Issue를 연결합니다 (`Close #N`).
3. `develop` / `main` 대상 PR은 Actions `Test`가 자동 실행됩니다.

---

## 2. Issue

GitHub **New Issue**에서 템플릿을 선택합니다.

| 유형 | 제목 prefix | Label | 용도 |
| --- | --- | --- | --- |
| Feature | `[Feature] ` | `Feature` | 신규 기능·API |
| Fix | `[Fix] ` | `Fix` | 버그 수정 |
| Refactor | `[Refactor] ` | `Refactor` | 동작 동일, 구조·가독성 개선 |
| Style | `[Style] ` | `Style` | 포맷·네이밍 등 동작 없는 스타일 정리 |
| Docs | `[Docs] ` | `Docs` | START / DEVELOPMENT_GUIDE 등 문서 |
| Chore | `[Chore] ` | `Chore` | 빌드·CI·의존성·설정 |

### 작성 요령

- `[Type] ` 뒤에 한글 요약을 구체적으로 적습니다. 예: `[Feature] 아지트 생성 API 추가`
- **작업할 내용**은 `- [ ]` 체크리스트로 나눕니다.
- Fix는 재현 / 원인 / 수정 항목을 구분합니다.
- 참고 링크(Figma, 로그, Notion)가 있으면 참고 칸에 적습니다.

> 레포에 Label(`Feature`, `Fix` 등)이 없으면 Issue 생성 시 라벨 적용이 실패할 수 있습니다. 최초 1회 GitHub Labels에 동일 이름으로 만들어 두세요.

---

## 3. 브랜치

```text
{type}/{issue-number}-{short-kebab-description}
```

| type | 예 |
| --- | --- |
| `feature` | `feature/12-agit-create-api` |
| `fix` | `fix/25-jwt-expire` |
| `refactor` | `refactor/40-persistence-mapper` |
| `style` | `style/18-dto-naming` |
| `docs` | `docs/31-start-guide` |
| `chore` | `chore/7-ci-gradle` |

- 베이스 브랜치는 **`develop`** 권장 (`main`은 안정/릴리즈).

---

## 4. 커밋 메시지

```text
{Type}: {변경 요약}
```

예:

```text
Feature: 아지트 생성 API 추가
Fix: JWT 토큰 만료 처리 수정
Docs: START.md 포트 안내 보완
Chore: test 프로필 H2 추가
```

- `{Type}`: `Feature` | `Fix` | `Refactor` | `Style` | `Docs` | `Chore`
- DDL 변경 시 `docs/sql/schema.sql`도 함께 커밋합니다.

---

## 4.1. Git Hooks (Gradle)

훅은 **컴파일/`./gradlew` 실행 시** 설치됩니다.

| Hook | 동작 | 목적 |
| --- | --- | --- |
| `commit-msg` | `scripts/commit-msg` → `.git/hooks/commit-msg` | 커밋 메시지 컨벤션 검증 (`Feature: ...` 등) |
| `pre-push` | `./gradlew test` | push 직전에만 테스트 (커밋 시 빌드/테스트 없음) |

```bash
./gradlew compileJava   # 또는 ./gradlew tasks — commit-msg 훅 설치 + pre-push 훅 반영
```

- 허용 메시지: `Feature|Fix|Refactor|Style|Docs|Chore: ` 뒤에 요약 (첫 줄)
- 우회는 `git commit --no-verify` / `git push --no-verify` (남용 금지)
- 클론 직후 Gradle을 한 번 실행해 훅을 설치하세요

---

## 5. Pull Request

### 제목

```text
[#{Issue Number}] {Type} : {작업 내용}
```

- `[#12] Feature : 로그인 API 구현`
- `[#25] Fix : JWT 토큰 만료 오류 수정`
- `[#31] Docs : README 수정`

### 본문

PR 템플릿 섹션을 모두 채웁니다.

| 섹션 | 내용 |
| --- | --- |
| 작업 내용 | 무엇을 왜 바꿨는지 |
| 관련 이슈 | `Close #N` |
| 변경 사항 | 주요 변경 체크리스트 |
| 테스트 | 로컬/CI 확인 항목 |

### 머지 전

- [ ] 제목·Type·이슈 번호 일치
- [ ] `Close #N` 연결
- [ ] CI `Test` 성공
- [ ] DDL 변경 시 `docs/sql/schema.sql` 포함

---

## 6. CI

[`.github/workflows/test.yml`](.github/workflows/test.yml)

- 트리거: `develop` / `main`에 대한 `pull_request`, `push`
- 실행: JDK 17 + `./gradlew test --no-daemon` (레포 루트)
- push 전 로컬에서도 `./gradlew test`로 확인합니다.

**API 명세:** [Gateway Swagger UI](http://192.168.10.144:8000/swagger-ui/index.html)에서 서비스별 OpenAPI를 통합 조회합니다. repo `docs/openapi.yaml` 커밋은 필수가 아닙니다.

---

## 7. 예시

```text
1. Issue  [Feature] 아지트 생성 API 추가  → #12
2. Branch feature/12-agit-create-api  (from develop)
3. Commit Feature: 아지트 생성 API 추가
4. PR     [#12] Feature : 아지트 생성 API 구현  / Close #12
5. CI     Test 통과 → 리뷰 → develop 머지
```

---

## 8. 다른 문서

| 문서 | 역할 |
| --- | --- |
| [START.md](START.md) | 템플릿 초기 설정 |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | 개발 아키텍처·DB |
| [DEVELOP_PC_DEPLOY.md](../plip-develop-env/DEVELOP_PC_DEPLOY.md) | Develop PC deploy · Gateway 통합 API 문서 |
| [AI_CODING_GUIDELINES.md](AI_CODING_GUIDELINES.md) | AI 코드 생성 규칙 |
| **GIT_CONVENTION.md** | Issue / 브랜치 / 커밋 / PR / CI |
