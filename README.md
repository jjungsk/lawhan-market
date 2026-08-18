# lawhan-market

lawhan.kr의 지식마켓(Market: 특허·상표·디자인권 매물 거래) 기능을 별도 도메인 + AWS 인프라로 신규 구축하는 프로젝트입니다.

## 저장소 구조

```
lawhan-market/
  backend/          # Spring Boot (Java) — M1에서 초기화 예정
  frontend/         # Next.js (TypeScript) — M7에서 초기화 예정
  infra/
    docker-compose.yml   # 뼈대만 존재, 서비스 정의는 M9에서
    nginx/nginx.conf     # 뼈대만 존재, 라우팅 규칙은 M9에서
  .github/workflows/     # 뼈대만 존재, 배포 워크플로우는 M10에서
  docs/                  # 요구사항/아키텍처/마일스톤 문서
  mockup/                # 기존 서비스 UI 스냅샷 (디자인 기준선, 계속 참조됨)
  cafe24-aws-migration-analysis.md
```

## 로컬 개발 실행법 (초안)

현재는 리포지토리 스캐폴딩(M0) 단계로, `backend/`와 `frontend/`가 비어 있습니다.

- **Backend**: Spring Boot 프로젝트 초기화 및 실행법은 M1에서 채워집니다 (`./gradlew bootRun` 예정).
- **Frontend**: Next.js 프로젝트 초기화 및 실행법은 M7에서 채워집니다 (`npm run dev` 예정).
- **전체 스택(Docker Compose)**: `infra/docker-compose.yml` 서비스 구성은 M9에서 채워집니다.
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
