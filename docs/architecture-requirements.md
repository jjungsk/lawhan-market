# lawhan-market 신규 서비스 — 요구사항 & 아키텍처 (v0.3)

작성일: 2026-08-18
상태: 주요 의사결정 완료, 스캐폴딩 착수 가능

---

## 1. 배경

lawhan.kr(카페24, 그누보드5, PHP 8.2 + MariaDB)에서 운영되던 "Market"(지식마켓: 특허·상표·디자인권 매물 거래) 기능을 **완전히 별도의 신규 도메인 + AWS 인프라**로 분리 신규 구축한다.

- 기존 `mockup/` 폴더는 실제 서비스에서 쓰던 목록(`index.html`)·상세(`detail.html`) 페이지를 PHP/DB 의존성 없이 정적 HTML+CSS로 캡처한 UI 기준선(baseline)이며, 이번 신규 구축의 디자인 출발점으로 그대로 재사용한다.
- 기존 lawhan.kr 본 사이트(About/Expertise/Notice 등)는 카페24에 그대로 유지하고 변경하지 않는다. 신규 도메인/서비스는 Market 기능 전용이다.
- 카페24 실질 비용은 약 8,000원/월(`cafe24-aws-migration-analysis.md` 참조)이며, 참고용 비교 자료일 뿐 신규 서비스의 예산 기준은 아니다.

## 2. 범위 (Scope)

**포함**
- 매물 목록/검색/정렬/카테고리 필터/페이징 — 실제 DB 연동
- 매물 상세 페이지
- 매물 문의폼 — 실제 제출(이메일 발송 + DB 저장, 관리자 화면에서도 확인 가능)
- 관리자(admin) 로그인 및 매물 CRUD (soft delete)
- 신규 도메인 구입/연결, AWS 인프라 구축, GitHub Actions 배포 파이프라인

**이번 범위 제외 (추후 단계)**
- 일반 회원가입/로그인 (설계는 확장 가능하게 해두되 구현은 이후)
- 결제/에스크로 기능
- **매물 일괄 등록 (Excel/CSV 업로드)** — 우선순위 낮음, 추가 기능으로 백로그에 기록만 해둠
- lawhan.kr 본 사이트의 다른 메뉴(About/Expertise/Professional/Notice/Contact)

## 3. 도메인 & 인프라

- 도메인: **로컬 구축을 완료한 뒤 구입 진행**. 개발 중에는 `localhost` 및 임시 EC2 IP/무료 서브도메인(예: `sslip.io`, `nip.io`)으로 SSL 없이 진행하고, 배포 검증 완료 후 실제 도메인을 구입해 Route 53에 연결한다.
- 배포 리전: 서울(ap-northeast-2)
- 트래픽 규모: 일 60~120 PV, 동시접속 한 자릿수 (기존 카페24 트래픽 실측 기준 추정) → 소규모 단일 인스턴스 구성으로 충분

### 추천 인프라 구성

| 구성요소 | 추천 | 비고 |
|---|---|---|
| 컴퓨트 | EC2 **t3.small** (2GB) 단일 인스턴스 | Spring Boot(JVM) + Next.js 두 프로세스를 함께 돌리므로 t3.micro(1GB)보다 상향 권장 |
| 프로세스 구성 | Docker Compose: `backend`(Spring Boot) + `frontend`(Next.js) + `db`(PostgreSQL) + `nginx`(리버스 프록시) | Nginx가 `/api/*` → backend:8080, 나머지 → frontend:3000 라우팅 |
| DB | 동일 EC2 내 PostgreSQL 컨테이너 | 매물 수·트래픽이 적어 RDS 분리는 이번엔 불필요. 추후 트래픽 증가 시 RDS로 분리 |
| 이미지 저장 | S3 (매물 이미지 업로드, 매물당 최대 5장) | |
| 이미지 전송 | CloudFront (S3 앞단, 월 1TB까지 무료 티어) | |
| 이메일 발송 | AWS SES (문의 접수 시 관리자에게 알림 메일) | |
| SSL | ACM 무료 인증서 (CloudFront/ALB) 또는 Let's Encrypt (Nginx 직접) | |
| 배포 | **GitHub Actions**: main 브랜치 push → 이미지 빌드 → EC2에 SSH로 pull & `docker compose up -d` | |

### 예상 월 비용 (참고, 환율 1,400원/$ 가정)

| 구성 | 비용/월 |
|---|---|
| EC2 t3.small 온디맨드 | 약 $19 (약 27,000원) |
| EC2 t3.small + 1년 Savings Plan | 약 $10~12 (약 15,000원) |
| S3 (이미지, 소용량) | 약 $0.5 |
| CloudFront / SES | 무료 티어 내 (저트래픽) |
| 도메인 등록 | 연 15,000~30,000원 (확장자에 따라 다름) |
| **합계 (Savings Plan 기준)** | **약 16,000~20,000원/월 + 도메인 연 비용** |

t3.micro 대비 월 몇천 원 더 들지만, JVM + Node.js 두 프로세스를 안정적으로 돌리기 위한 최소 상향으로 판단.

## 4. 기술 스택 (확정)

### Backend: Spring Boot (Java)
- Spring Web (REST API), Spring Data JPA + PostgreSQL, Spring Security (admin 인증/인가)
- 익숙한 스택으로 개발 속도·유지보수 면에서 합리적. Spring Security로 `/admin/**` API에 대한 인증 가드를 표준적으로 구성 가능.
- 유의점: JVM 메모리 사용량이 Node 대비 크므로 인스턴스를 t3.small로 상향(§3 참고).

### Frontend: Next.js (TypeScript, App Router)
- 바닐라 JS 대신 Next.js 채택. 어드민 CRUD 폼(등록/수정, 이미지 업로드 미리보기, 유효성 검사), 목록의 필터/정렬/페이징 상태 관리에서 컴포넌트 재사용과 타입 안정성 이점이 큼.
- Spring Boot REST API를 fetch로 호출하는 순수 프론트 역할 (Next.js의 API Routes는 사용하지 않음).
- 기존 mockup이 이미 순수 HTML/CSS 구조라 컴포넌트 이관 부담이 적음.
- SSR을 활용해 목록/상세 페이지 SEO 확보.

### 프론트 반응형 (필수)
- 모바일에서도 자주 조회되므로 반응형 대응 필수.
- 참고로 기존 mockup CSS를 확인한 결과, 사이트 공통 레이아웃(`lawhan_css.css`)에는 이미 1024px/767px 기준 반응형 브레이크포인트가 다수(57개) 적용되어 있고, Market 전용 스타일(`market.css`)에도 1100px/900px/700px 기준으로 카드 그리드·사이드바·상세 레이아웃·문의폼이 반응형 처리되어 있음. 즉 **완전히 새로 만들 필요 없이 기존 반응형 기반 위에서 Next.js로 이관 + 모바일 실기기/뷰포트 QA를 통한 보완**이면 충분함.
- 이관 시 체크리스트: 카드 그리드 1~2열 전환, 사이드바 접힘/드롭다운화, 어드민 CRUD 폼·테이블의 모바일 대응(관리자도 모바일에서 쓸 가능성 고려), 이미지 갤러리 터치 스와이프.

### 저장소 구조: 모노레포 (확정)
```
lawhan-market/
  backend/          # Spring Boot (Gradle or Maven)
  frontend/          # Next.js (TypeScript)
  infra/
    docker-compose.yml
    nginx/
      nginx.conf
  .github/
    workflows/
      deploy.yml
  docs/
    architecture-requirements.md   # 본 문서
```
- 모노레포로 진행 — 소규모 2인 이하 운영에서 PR/이슈/버전 태그를 한 곳에서 관리하는 이점이 배포 파이프라인 관리 부담보다 큼.
- Docker Compose로 backend + frontend + db + nginx를 한 EC2에서 함께 기동.

## 5. DB 마이그레이션 도구: Flyway (추천/확정)

Flyway를 추천한다.

- 순수 SQL 기반이라 러닝커브가 낮고, Spring Boot 자동 설정(`spring-boot-starter-flyway` 또는 내장 지원)과 궁합이 좋음.
- 이 프로젝트 규모(테이블 3~4개, 스키마 변경 빈도 낮음)에서는 Liquibase의 다중 포맷(XML/YAML/JSON) 지원이나 자동 rollback DSL 같은 고급 기능이 필요하지 않음 — 오히려 불필요한 학습 비용.
- admin 계정 시딩(§7)도 Flyway 버전 마이그레이션 파일로 자연스럽게 관리 가능.
- 마이그레이션 파일 규칙: `backend/src/main/resources/db/migration/V1__init_schema.sql`, `V2__seed_admin.sql` 형태로 순차 관리.

## 6. 데이터 모델 (초안)

```
users
  id, email, password_hash, role(enum: admin, member[미래용]), created_at

listings (매물)
  id, title, category(enum: 특허/실용신안권, 상표권, 디자인권, 기술 라이센싱),
  price(nullable, "가격 협의" 대응), status(enum: 판매중, 협의중, 판매완료),
  app_number(출원번호), reg_number(등록번호), summary, content,
  thumbnail_url, images(최대 5장, 1:N 테이블 또는 JSON 배열),
  deleted_at(nullable, soft delete), created_at, updated_at, created_by(FK users)

inquiries (문의)
  id, listing_id(FK), type(매수/라이센싱/기타), company, name, email, phone,
  price_hope, content, agreed_at, created_at, status(신규/확인/응답완료)
```

soft delete: `listings.deleted_at`으로 처리. 목록/상세 조회 쿼리는 기본적으로 `deleted_at IS NULL` 필터. 데이터가 많아지면 별도 아카이브 테이블로 이관하는 방식으로 유연하게 전환 가능하도록 설계(지금은 컬럼 하나로 충분).

## 7. 기능 요구사항

### Public (일반 방문자)
- 매물 목록: 카테고리 필터, 검색(제목/내용), 정렬(최신순/가격순), 페이징 — 전부 실 DB 연동
- 매물 상세: 정보 표시, 이미지 갤러리(최대 5장), 연관상품
- 문의폼: 제출 시 (1) DB 저장 + (2) AWS SES로 관리자 이메일 알림, 둘 다 수행

### Admin
- `/admin/login`: DB에 사전 등록된 admin 계정으로 로그인 (세션 또는 JWT)
- `/admin/listings`: 매물 목록, 등록/수정/삭제(soft delete), 이미지 업로드(대표 1장 포함 최대 5장, S3)
- `/admin/inquiries`: 문의 내역 조회, 상태 변경(신규/확인/응답완료)
- 인증되지 않은 접근은 `/admin/login`으로 리다이렉트 (Spring Security + 프론트 라우트 가드 이중 체크)

### 백로그 (우선순위 낮음, 이번 범위 아님)
- 매물 일괄 등록 (Excel/CSV 업로드) — 어드민에서 파일 업로드 시 다건 매물 일괄 생성. 추후 매물 수가 늘어날 때 재검토.

## 8. 인증 설계

- 1단계(이번 범위): admin 계정만 DB에 사전 등록(DML 시딩), 로그인 기능 구현. `role` 컬럼으로 admin 여부 판별, admin이면 매물 CRUD 전 권한.
- **DML 시딩 방식(확정)**: Flyway 버전 마이그레이션 파일(예: `V2__seed_admin.sql`)에 admin 계정 INSERT를 포함. 단, 아래 두 가지는 지킨다.
  1. 비밀번호는 평문이 아니라 **BCrypt 해시**로 미리 생성해 SQL에 넣는다 (Spring Security의 `PasswordEncoder`로 로컬에서 해시값만 생성).
  2. `ON CONFLICT (email) DO NOTHING` 등으로 **재실행해도 중복 삽입되지 않게** 작성한다.
  - 저장소가 향후 외부에 공개될 가능성이 있다면, 이 시딩 SQL은 초기 계정일 뿐이며 **배포 직후 반드시 비밀번호를 변경**하는 것을 운영 체크리스트에 포함한다.
- 2단계(향후): 일반 회원가입 오픈. `role='member'`로 가입, member는 조회/문의만 가능하고 CRUD 권한 없음. 인증 체계(users 테이블, role 기반 체크)는 1단계와 동일하게 재사용 — 구조 변경 불필요.
- 어드민 화면은 `/admin` 하위 별도 라우트로 분리 (일반 화면과 완전히 독립된 레이아웃/가드). 게시판(목록) 페이지에 인라인 CRUD를 넣지 않음 — 관심사 분리 및 향후 회원 기능 확장 시 영향 최소화를 위함.

## 9. 배포 파이프라인 (GitHub Actions)

1. `main` 브랜치 push (또는 PR merge) 트리거
2. backend/frontend 각각 Docker 이미지 빌드 (또는 변경된 쪽만 빌드)
3. 이미지를 레지스트리(예: GHCR)에 push
4. EC2에 SSH 접속 → `docker compose pull && docker compose up -d`
5. (선택) 헬스체크 후 실패 시 롤백 스크립트

세부 워크플로우 파일(.github/workflows/*.yml)은 저장소 구조 확정 후 작성.

## 10. 의사결정 요약 (Decision Log)

| 항목 | 결정 |
|---|---|
| 도메인 범위 | lawhan.kr과 완전 별도 신규 도메인, Market 기능 전용 |
| 도메인 구입 시점 | 로컬 구축 완료 후 구입 |
| 백엔드 | Spring Boot (Java) + Spring Data JPA + Spring Security |
| 프론트엔드 | Next.js (TypeScript, App Router), 반응형 필수 |
| DB | PostgreSQL |
| 마이그레이션 도구 | Flyway |
| 저장소 구조 | 모노레포 (`backend/`, `frontend/`, `infra/`, `docs/`) |
| 인프라 | EC2 t3.small 단일 인스턴스, Docker Compose (backend+frontend+db+nginx) |
| 배포 자동화 | GitHub Actions → EC2 배포 |
| admin 인증 | DB 사전 등록(Flyway DML 시딩, BCrypt 해시), role 컬럼으로 향후 회원 기능 확장 |
| 이미지 | 매물당 대표 1장 포함 최대 5장, S3 저장 |
| 매물 삭제 | soft delete (`deleted_at`) |
| 문의 알림 | DB 저장 + AWS SES 이메일 알림 동시 |
| 매물 일괄 등록(Excel/CSV) | 백로그 (이번 범위 아님) |

## 11. 다음 실행 단계

주요 의사결정이 모두 끝났으므로, 이제 리포지토리 스캐폴딩(폴더 구조, Spring Boot/Next.js 초기 프로젝트, Docker Compose, Flyway 초기 마이그레이션, GitHub Actions 워크플로우 뼈대)을 시작할 수 있는 상태입니다. 착수 시점과 순서는 사용자 확인 후 진행합니다.

---
*이 문서는 이후 논의에 따라 갱신됩니다.*
