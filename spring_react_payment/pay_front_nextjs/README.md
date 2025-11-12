# Pay Front Next.js

토스 페이먼츠 결제 시스템 프론트엔드 (Next.js 버전)

React + TypeScript + Next.js + Tailwind CSS + shadcn/ui 프로젝트

## 기술 스택

- **Next.js 16** - React 프레임워크 (App Router)
- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Tailwind CSS** - 유틸리티 CSS 프레임워크
- **shadcn/ui** - UI 컴포넌트 라이브러리
- **Axios** - HTTP 클라이언트
- **Toss Payments SDK** - 결제 연동
- **Zustand** - 상태 관리
- **React Hook Form + Zod** - 폼 관리 및 검증

## 시작하기

### 필수 요구사항

- Node.js 18+
- pnpm

### 설치

```bash
pnpm install
```

### 개발 서버 실행

```bash
pnpm dev
```

개발 서버는 `http://localhost:3000`에서 실행됩니다.

### 빌드

```bash
pnpm build
```

### 프로덕션 실행

```bash
pnpm start
```

## 프로젝트 구조

```
pay_front_nextjs/
├── app/                    # Next.js App Router
│   ├── layout.tsx         # 루트 레이아웃
│   ├── page.tsx           # 홈 페이지
│   ├── globals.css        # 전역 스타일
│   └── (routes)/          # 라우트 페이지들
├── components/
│   ├── ui/                # shadcn/ui 컴포넌트
│   └── ...                # 커스텀 컴포넌트
├── hooks/                 # Custom Hooks
├── lib/
│   ├── api.ts             # Axios 인스턴스
│   ├── services/          # API 서비스
│   └── utils.ts           # 유틸리티 함수
├── store/                 # Zustand 스토어
└── types/                 # TypeScript 타입 정의
```

## 주요 기능

- 사용자 로그인 및 회원가입
- 결제 초기화 및 승인
- 결제 성공/실패 페이지
- JWT 토큰 기반 인증
- 토스 페이먼츠 결제 연동
- 관리자 대시보드

## 환경 변수 설정

프로젝트 루트에 `.env.local` 파일을 생성하고 다음 내용을 추가하세요:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:9000
NEXT_PUBLIC_TOSS_CLIENT_KEY=your_toss_client_key
NEXT_PUBLIC_TOSS_CUSTOMER_KEY=your_customer_key
```

## 백엔드 연동

프로젝트는 Spring Boot 백엔드(`toas_payment2`)와 연동됩니다.

### API 사용 예제

```typescript
import api from '@/lib/api'

// GET 요청
const response = await api.get('/api/v1/endpoint')

// POST 요청
const response = await api.post('/api/v1/endpoint', { data })
```

### 인증

JWT 토큰은 `localStorage`에 저장되며, 모든 API 요청에 자동으로 포함됩니다.

## shadcn/ui 컴포넌트 추가

새로운 shadcn/ui 컴포넌트를 추가하려면:

```bash
npx shadcn@latest add [component-name]
```

예: `npx shadcn@latest add card`

## Next.js App Router

이 프로젝트는 Next.js 16의 App Router를 사용합니다:

- `app/` 디렉토리: 라우트 및 레이아웃
- `app/page.tsx`: 홈 페이지 (`/`)
- `app/login/page.tsx`: 로그인 페이지 (`/login`)
- `app/(auth)/`: 인증 관련 라우트 그룹

## 추가 리소스

- [Next.js 문서](https://nextjs.org/docs)
- [React 문서](https://react.dev/)
- [Tailwind CSS 문서](https://tailwindcss.com/)
- [shadcn/ui 문서](https://ui.shadcn.com/)
- [토스 페이먼츠 문서](https://docs.tosspayments.com/)
