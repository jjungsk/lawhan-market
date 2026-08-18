# lawhan-market 마일스톤 & 태스크 브레이크다운 (v0.1)

작성일: 2026-08-18
참조 문서: `architecture-requirements.md` (v0.3)

작업 방식: 각 마일스톤(또는 인접한 2~3개 묶음)을 VS Code에서 하나의 독립 세션으로 처리. 세션 시작 시 `architecture-requirements.md`를 컨텍스트로 제공하고, 마일스톤 완료 시 커밋 후 세션 종료. 다음 세션은 git 상태 기준으로 새로 시작.

이 PM 세션에서의 역할: 마일스톤 완료 보고를 받으면 → 스펙 대비 검토, Decision Log/문서 갱신, 다음 마일스톤 우선순위 조정, 막힌 지점(블로커) 논의.

---

## M0. 리포지토리 스캐폴딩
- 모노레포 폴더 구조 생성 (`backend/`, `frontend/`, `infra/`, `docs/`, `.github/workflows/`)
- `docs/architecture-requirements.md` 리포에 복사
- 루트 `README.md` (로컬 개발 실행법 초안)
- `.gitignore` (Java/Node/OS 공통)
- **완료 기준**: 빈 프로젝트라도 `git clone` 후 구조가 문서(§4 저장소 구조)와 일치

## M1. Backend 기본 골격
- Spring Boot 프로젝트 초기화 (Gradle 추천 — Maven보다 설정이 간결)
- 의존성: Spring Web, Spring Data JPA, Spring Security, Flyway, PostgreSQL Driver, Validation
- `application.yml` (local / prod 프로파일 분리)
- Flyway `V1__init_schema.sql` (users, listings, inquiries 테이블 — 아키텍처 문서 §6 데이터 모델 기준)
- Flyway `V2__seed_admin.sql` (BCrypt 해시 admin 계정, `ON CONFLICT DO NOTHING`)
- 로컬 Docker Compose로 Postgres만 띄워서 마이그레이션 동작 확인
- **완료 기준**: `./gradlew bootRun`으로 기동, Flyway 마이그레이션이 자동 적용되고 admin 계정 1건 확인 가능

## M2. Public API — 매물 목록/상세
- `GET /api/listings` — 카테고리 필터, 검색(제목/내용), 정렬(최신순/가격순), 페이징
- `GET /api/listings/{id}` — 상세 (soft delete된 매물은 404)
- `GET /api/listings/{id}/related` — 연관상품(같은 카테고리 등)
- DTO/엔티티 분리, 페이징 응답 포맷 확정 (예: `{items, total, page, size}`)
- **완료 기준**: Postman/curl로 목록·상세·필터·정렬·페이징 전부 검증 가능

## M3. 문의 API + 이메일 알림
- `POST /api/listings/{id}/inquiries` — 문의 저장 (DB)
- AWS SES 연동 (로컬은 SES 대신 로그 출력 또는 Mailhog 같은 로컬 SMTP로 대체 가능)
- 문의 저장 실패/이메일 발송 실패 시 사용자 응답 처리 정책 결정 (예: DB 저장은 성공했는데 메일만 실패한 경우 사용자에겐 성공으로 응답 + 내부 재시도/알림)
- **완료 기준**: 문의 제출 시 DB row 생성 + (로컬은 로그로) 알림 트리거 확인

## M4. Admin 인증 (Spring Security)
- `/admin/**` API에 대한 인증 가드 (세션 기반 또는 JWT — 세션이 이 규모엔 더 단순, 추천)
- `POST /api/admin/login`, `POST /api/admin/logout`
- 로그인 실패/잠금 정책은 최소한만 (무제한 시도 방지 정도)
- **완료 기준**: admin 계정으로 로그인 성공 시 세션 쿠키 발급, 미로그인 상태에서 `/admin/**` 호출 시 401/403

## M5. Admin 매물 CRUD API
- `POST /api/admin/listings` (등록), `PUT /api/admin/listings/{id}` (수정), `DELETE /api/admin/listings/{id}` (soft delete)
- `GET /api/admin/listings` (삭제된 것 포함 전체 조회 옵션)
- 유효성 검사 (필수 필드, 카테고리 enum, 가격 형식)
- **완료 기준**: admin 세션으로 CRUD 전 과정 curl/Postman 검증, soft delete 후 public API에서 조회 안 됨 확인

## M6. S3 이미지 업로드
- 매물당 대표 1장 포함 최대 5장 제약 서버측 검증
- `POST /api/admin/listings/{id}/images` (업로드), 삭제 API
- 로컬 개발은 LocalStack 또는 임시 로컬 디스크 저장으로 대체 가능 (배포 시 S3 전환)
- **완료 기준**: 이미지 업로드 후 URL이 매물 상세 응답에 포함되고, 6번째 업로드 시도는 거부됨

## M7. Frontend — Next.js 셋업 + Public 페이지 이관
- Next.js(TypeScript, App Router) 프로젝트 초기화
- mockup의 `index.html`/`detail.html` 구조 및 `market.css` 룩앤필을 컴포넌트로 이관
- 목록 페이지: 카테고리 필터, 검색, 정렬, 페이징 — M2 API 연동
- 상세 페이지: 이미지 갤러리(최대 5장), 문의폼 — M3 API 연동
- 반응형 QA (아키텍처 문서 §4 체크리스트 기준: 카드 그리드, 사이드바, 문의폼, 이미지 갤러리 스와이프)
- **완료 기준**: 실제 API 데이터로 목록/상세/문의 제출이 동작하고, 모바일 뷰포트에서 레이아웃 깨짐 없음

## M8. Frontend — Admin 대시보드
- `/admin/login` 페이지
- `/admin/listings` (목록 + 등록/수정/삭제 폼, 이미지 업로드 미리보기)
- `/admin/inquiries` (문의 내역 조회, 상태 변경)
- 라우트 가드 (미인증 시 `/admin/login`으로 리다이렉트)
- **완료 기준**: admin 로그인 후 매물 등록→목록에 반영→수정→soft delete 전 과정을 브라우저에서 수행 가능

## M9. Docker Compose 통합
- `infra/docker-compose.yml`: backend + frontend + db + nginx
- `infra/nginx/nginx.conf`: `/api/*` → backend, 나머지 → frontend
- 로컬에서 `docker compose up`으로 전체 스택 기동 검증
- **완료 기준**: 로컬 임시 도메인(`localhost` 또는 `*.sslip.io`)으로 전체 플로우(조회→문의→admin CRUD) 통합 테스트 통과

## M10. GitHub Actions 배포 파이프라인
- `.github/workflows/deploy.yml`: main push 시 이미지 빌드 → GHCR push → EC2 SSH 배포
- EC2 인스턴스 준비 (t3.small), Docker/Docker Compose 설치, 시크릿 관리(GitHub Secrets ↔ EC2 환경변수)
- **완료 기준**: main에 push하면 자동으로 EC2에 반영되는 것을 1회 이상 확인

## M11. 도메인 연결 + SSL
- 도메인 구입, Route 53 연결
- ACM 또는 Let's Encrypt SSL 적용
- **완료 기준**: 실제 도메인으로 HTTPS 접속 및 전체 기능 정상 동작

## M12. 최종 QA / 오픈 준비
- 반응형 재확인 (실기기 포함)
- admin 최초 비밀번호 변경
- 기존 mockup 대비 기능 누락 여부 체크 (검색/정렬/필터/페이징/문의/연관상품)
- 백업 정책 최소 확인 (DB 스냅샷 등)

---

## 백로그 (이번 범위 아님, 추후 별도 마일스톤)
- 매물 일괄 등록 (Excel/CSV 업로드)
- 일반 회원가입/로그인 (member 역할 오픈)
- RDS 분리, CloudFront 정식 적용 등 트래픽 증가 대응

---
*이 문서는 진행 상황에 따라 갱신됩니다. VS Code 세션에서 마일스톤 완료 시 이 세션에 보고해주시면 체크 표시 및 다음 우선순위를 정리해드립니다.*
