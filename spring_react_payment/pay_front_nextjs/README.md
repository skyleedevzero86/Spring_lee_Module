# 토스 페이먼츠 결제 시스템

Next.js 16을 사용한 토스 페이먼츠 결제 시스템 프론트엔드입니다.

## 주요 기능

- 코드 스플리팅 및 동적 import
- 재시도 로직 (exponential backoff)
- 보안 강화 (토큰 관리, CSP 헤더)
- 캐싱 전략 (React Query)
- 관찰성 확보 (로깅, 에러 추적, 성능 모니터링)
- E2E 테스트 (Playwright)
- 프로덕션 최적화

## 기술 스택

- **Framework**: Next.js 16
- **Language**: TypeScript
- **State Management**: Zustand, React Query
- **Form Handling**: React Hook Form + Zod
- **Styling**: Tailwind CSS
- **Testing**: Jest, React Testing Library, Playwright
- **HTTP Client**: Axios

## 프로젝트 구조

```
pay_front_nextjs/
├── src/
│   ├── domain/              # 도메인 레이어
│   │   ├── types/          # 타입 정의
│   │   └── validators/     # 검증 스키마
│   ├── infrastructure/      # 인프라 레이어
│   │   ├── api/            # API 클라이언트
│   │   └── http/           # HTTP 클라이언트
│   ├── application/         # 애플리케이션 레이어
│   │   └── services/       # 비즈니스 로직
│   ├── store/              # 상태 관리
│   ├── hooks/              # 커스텀 훅
│   ├── components/         # UI 컴포넌트
│   │   ├── common/         # 공통 컴포넌트
│   │   ├── member/         # 회원 관련 컴포넌트
│   │   └── payment/        # 결제 관련 컴포넌트
│   ├── lib/                # 유틸리티
│   │   ├── logger/         # 로깅 시스템
│   │   ├── monitoring/     # 모니터링 시스템
│   │   └── config/         # 설정
│   └── app/                # Next.js 페이지
├── e2e/                    # E2E 테스트
└── docs/                   # 문서
```

## 시작하기

### 설치

```bash
pnpm install
```

### 환경 변수 설정

`.env.local` 파일을 생성하고 다음 내용을 설정:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_ERROR_TRACKING_ENABLED=false
NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED=false
```

### 개발 서버 실행

```bash
pnpm dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어 확인하세요.

## 스크립트

- `pnpm dev`: 개발 서버 시작
- `pnpm dev:clean`: 캐시 정리 후 개발 서버 시작 (문제 발생 시 사용)
- `pnpm build`: 프로덕션 빌드
- `pnpm start`: 프로덕션 서버 시작
- `pnpm lint`: 코드 린팅
- `pnpm clean`: 빌드 캐시 정리 (.next, .swc, node_modules/.cache)
- `pnpm test`: 단위 테스트 실행
- `pnpm test:watch`: 단위 테스트 감시 모드
- `pnpm test:coverage`: 커버리지 리포트 생성
- `pnpm test:e2e`: E2E 테스트 실행
- `pnpm test:e2e:ui`: E2E 테스트 UI 모드
- `pnpm analyze`: 번들 분석

## 문제 해결

### 개발 서버 에러 발생 시

1. **캐시 정리**:

   ```bash
   pnpm clean
   ```

2. **캐시 정리 후 개발 서버 재시작**:
   ```bash
   pnpm dev:clean
   ```

### Windows에서 파일 시스템 에러 발생 시

Windows에서 파일 잠금 문제가 발생하면:

- 개발 서버를 종료한 후 `pnpm clean` 실행
- 또는 `pnpm dev:clean` 사용하여 자동으로 캐시 정리 후 시작

## 주요 기능

### 회원 관리

- 회원가입
- 회원 조회
- 회원 검색
- 비밀번호 재설정

### 결제 관리

- 결제 생성
- 결제 승인
- 결제 상태 조회
- 결제 이력 조회
- 결제 상세 조회
- 환불 처리

## 문서

- [API 문서](./docs/API.md)
- [컴포넌트 문서](./docs/COMPONENTS.md)
- [모니터링 문서](./docs/MONITORING.md)
- [배포 문서](./docs/DEPLOYMENT.md)

## 아키텍처 원칙

### 클린 아키텍처

1. **Domain Layer**: 비즈니스 로직과 타입 정의
2. **Infrastructure Layer**: 외부 시스템과의 통신 (API, HTTP)
3. **Application Layer**: 유스케이스 및 서비스 로직
4. **Presentation Layer**: UI 컴포넌트 및 페이지

### 보안 고려사항

- 입력 검증: Zod 스키마를 통한 클라이언트 사이드 검증
- XSS 방지: React의 기본 이스케이핑 활용
- CSP 헤더: Content Security Policy 설정
- 토큰 관리: 암호화된 토큰 저장 및 자동 갱신
- 환경 변수 검증: Zod를 통한 환경 변수 검증

## 테스트

### 단위 테스트

```bash
pnpm test
pnpm test:watch
pnpm test:coverage
```

### E2E 테스트

```bash
pnpm test:e2e
pnpm test:e2e:ui
```

## 빌드 및 배포

```bash
pnpm build
pnpm start
```

자세한 배포 가이드는 [배포 문서](./docs/DEPLOYMENT.md)를 참조하세요.
