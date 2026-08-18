# lawhan-market

lawhan.kr의 지식마켓(Market: 특허·상표·디자인권 매물 거래) 기능을 별도 도메인 + AWS 인프라로 신규 구축하는 프로젝트입니다.

> 이 저장소는 public입니다 (포트폴리오 공개 및 협업자 접근 목적).

## 저장소 구조

```
lawhan-market/
  backend/          # Spring Boot (Java, Gradle) — 스키마/마이그레이션까지 완료(M1), API는 M2~M6에서
  frontend/         # Next.js (TypeScript) — M7에서 초기화 예정
  infra/
    docker-compose.yml   # 뼈대만 존재, 서비스 정의는 M9에서
    nginx/nginx.conf     # 뼈대만 존재, 라우팅 규칙은 M9에서
  .github/workflows/     # 뼈대만 존재, 배포 워크플로우는 M10에서
  docs/                  # 요구사항/아키텍처/마일스톤 문서
  mockup/                # 기존 서비스 UI 스냅샷 (디자인 기준선, 계속 참조됨)
```

## 로컬 개발 실행법 (초안)

- **Backend** (Spring Boot, Gradle): DB를 먼저 띄운 뒤 애플리케이션을 기동합니다.

  ```bash
  # 1) 로컬 Postgres 기동 (infra/docker-compose.yml의 db 서비스만 사용)
  docker compose -f infra/docker-compose.yml up -d db

  # 2) 애플리케이션 기동 (local 프로파일이 기본 활성화됨)
  cd backend
  ./gradlew bootRun
  ```

  기동 시 Flyway가 `db/migration`의 마이그레이션을 자동 적용하고(스키마 생성 + admin 계정 시딩),
  기본 포트는 `8080`입니다. `application-local.yml` / `application-prod.yml`로 프로파일이 분리되어
  있으며, prod 프로파일은 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 환경변수로 접속 정보를 주입받습니다.
  admin 계정 로그인 API는 M4에서 추가됩니다.

- **Frontend**: Next.js 프로젝트 초기화 및 실행법은 M7에서 채워집니다 (`npm run dev` 예정).
- **전체 스택(Docker Compose)**: backend/frontend/nginx 서비스 정의는 M9에서 채워집니다.
- **UI 기준선 확인**: 실제 페이지 구현 전까지는 `mockup/` 폴더의 정적 스냅샷으로 디자인을 참고할 수 있습니다.

  ```bash
  cd mockup
  python3 -m http.server 8899
  # http://localhost:8899 (목록), http://localhost:8899/detail.html (상세)
  ```

## 문서

- [요구사항 & 아키텍처](docs/architecture-requirements.md) — 스택, 인프라, 데이터 모델, 기능 요구사항 전체
- [마일스톤 & 태스크 브레이크다운](docs/milestones.md) — 작업 순서 및 각 마일스톤 완료 기준
- [카페24 → AWS 마이그레이션 분석](docs/cafe24-aws-migration-analysis.md) — 참고용 비용 비교 자료

## 백로그 (우선순위 낮음 / 추후 진행)

- **README에 스크린샷·배지 추가** — 프론트(M7~M8) 완성 후 실제 화면 캡처 및 빌드/배포 상태 배지(GitHub Actions 등) 추가
- **매물 일괄 등록 (Excel/CSV 업로드)** — 어드민 기능, `docs/milestones.md` 참고
- **일반 회원가입/로그인 (member 역할 오픈)**
- **RDS 분리 / CloudFront 정식 적용 등 트래픽 증가 대응**
- **LICENSE 파일 추가 여부 결정** — public 저장소이므로 포트폴리오 목적에 맞는 라이선스(MIT 등) 검토
