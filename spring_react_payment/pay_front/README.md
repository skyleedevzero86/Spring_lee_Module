# Pay Front

React + TypeScript + Vite + Tailwind CSS + shadcn/ui + Zustand 프로젝트

## 기술 스택

- **React 19** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빌드 도구 (Rolldown 포함)
- **Tailwind CSS** - 유틸리티 CSS 프레임워크
- **shadcn/ui** - UI 컴포넌트 라이브러리
- **Zustand** - 상태 관리
- **Axios** - HTTP 클라이언트 (Spring 연동용)

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
│   │   └── ui/          # shadcn/ui 컴포넌트
│   ├── lib/
│   │   ├── api.ts       # Axios 인스턴스 (Spring 연동)
│   │   └── utils.ts     # 유틸리티 함수
│   ├── store/
│   │   └── useStore.ts  # Zustand store
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css        # Tailwind CSS
├── components.json       # shadcn/ui 설정
├── tailwind.config.ts   # Tailwind 설정
├── vite.config.ts       # Vite 설정 (Rolldown 포함)
└── tsconfig.json        # TypeScript 설정
```

## Spring 연동

프로젝트는 Spring Boot 백엔드(`toas_payment2`)와 연동할 수 있도록 설정되어 있습니다.

### API 프록시 설정

`vite.config.ts`에서 API 프록시가 설정되어 있습니다:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
}
```

### API 사용 예제

```typescript
import api from '@/lib/api'

// GET 요청
const response = await api.get('/endpoint')

// POST 요청
const response = await api.post('/endpoint', { data })
```

## shadcn/ui 컴포넌트 추가

새로운 shadcn/ui 컴포넌트를 추가하려면:

```bash
npx shadcn@latest add [component-name]
```

예: `npx shadcn@latest add card`

## 상태 관리 (Zustand)

Zustand store는 `src/store/useStore.ts`에 정의되어 있습니다.

사용 예제:

```typescript
import { useStore } from '@/store/useStore'

function MyComponent() {
  const { count, increment } = useStore()
  return <button onClick={increment}>{count}</button>
}
```

## 추가 리소스

- [Vite 문서](https://vite.dev/)
- [React 문서](https://react.dev/)
- [Tailwind CSS 문서](https://tailwindcss.com/)
- [shadcn/ui 문서](https://ui.shadcn.com/)
- [Zustand 문서](https://zustand-demo.pmnd.rs/)

