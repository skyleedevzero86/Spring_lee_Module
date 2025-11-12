# Pay Front

토스 페이먼츠 결제 시스템 프론트엔드

React + TypeScript + Vite + Tailwind CSS + shadcn/ui 프로젝트

## 기술 스택

- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빌드 도구
- **React Router DOM** - 라우팅
- **Tailwind CSS** - 유틸리티 CSS 프레임워크
- **shadcn/ui** - UI 컴포넌트 라이브러리
- **Axios** - HTTP 클라이언트
- **Toss Payments SDK** - 결제 연동
- **Zustand** - 상태 관리

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

### 미리보기

```bash
pnpm preview
```

## 프로젝트 구조

```
pay_front/
├── src/
│   ├── components/
│   │   └── ui/              # shadcn/ui 컴포넌트
│   ├── hooks/
│   │   ├── useAuth.ts       # 인증 관련 Hook
│   │   ├── usePayment.ts    # 결제 관련 Hook
│   │   └── usePaymentWidget.ts  # 결제 위젯 Hook
│   ├── lib/
│   │   ├── api.ts           # Axios 인스턴스
│   │   ├── services/        # API 서비스
│   │   │   ├── authService.ts
│   │   │   └── paymentService.ts
│   │   └── utils.ts         # 유틸리티 함수
│   ├── pages/
│   │   ├── HomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── PayPage.tsx
│   │   ├── PaySuccessPage.tsx
│   │   └── FailPage.tsx
│   ├── store/
│   │   └── useStore.ts      # Zustand store
│   ├── types/
│   │   └── api.ts           # API 타입 정의
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css            # Tailwind CSS
├── components.json          # shadcn/ui 설정
├── tailwind.config.ts       # Tailwind 설정
├── vite.config.ts           # Vite 설정
└── tsconfig.json           # TypeScript 설정
```

## 주요 기능

- 사용자 로그인 및 회원가입
- 결제 초기화 및 승인
- 결제 성공/실패 페이지
- JWT 토큰 기반 인증
- 토스 페이먼츠 결제 연동

## 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
VITE_API_BASE_URL=http://localhost:9000
VITE_TOSS_CLIENT_KEY=your_toss_client_key
```

## 백엔드 연동

프로젝트는 Spring Boot 백엔드(`toas_payment2`)와 연동됩니다.

### API 프록시 설정

`vite.config.ts`에서 API 프록시가 설정되어 있습니다:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:9000',
      changeOrigin: true,
    },
  },
}
```

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

## 페이지 구조

- **HomePage** (`/`) - 메인 페이지
- **LoginPage** (`/login`) - 로그인 페이지
- **PayPage** (`/pay`) - 결제 페이지
- **PaySuccessPage** (`/success`) - 결제 성공 페이지
- **FailPage** (`/pay/fail`) - 결제 실패 페이지

## Custom Hooks

### useAuth
인증 관련 로직을 처리하는 Hook입니다.

```typescript
import { useAuth } from '@/hooks/useAuth'

const { login, logout, loading, error } = useAuth()
```

### usePayment
토스 페이먼츠 결제를 처리하는 Hook입니다.

```typescript
import { usePayment } from '@/hooks/usePayment'

const { requestPayment, confirmPayment, loading, error } = usePayment()
```

## 추가 리소스

- [Vite 문서](https://vite.dev/)
- [React 문서](https://react.dev/)
- [React Router 문서](https://reactrouter.com/)
- [Tailwind CSS 문서](https://tailwindcss.com/)
- [shadcn/ui 문서](https://ui.shadcn.com/)
- [토스 페이먼츠 문서](https://docs.tosspayments.com/)

