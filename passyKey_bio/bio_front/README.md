# PassyKey Bio Frontend

Next.js + TypeScript로 구현된 WebAuthn/Passkey 인증 시스템의 프론트엔드입니다.

## 기술 스택

- **Next.js 14** (App Router)
- **TypeScript**
- **React 18**

## 시작하기

### 설치

```bash
npm install
```

### 개발 서버 실행

```bash
npm run dev
```

개발 서버는 포트 80에서 실행됩니다. 브라우저에서 [http://localhost](http://localhost)를 열어 확인하세요.

### 빌드

```bash
npm run build
```

### 프로덕션 실행

```bash
npm start
```

## 프로젝트 구조

```
bio_front/
├── app/                    # Next.js App Router 페이지
│   ├── page.tsx           # 홈 페이지
│   ├── login/             # 로그인 페이지
│   ├── register/          # 회원가입 페이지
│   ├── dashboard/         # 대시보드 페이지
│   ├── layout.tsx         # 루트 레이아웃
│   └── globals.css        # 전역 스타일
├── components/            # 재사용 가능한 컴포넌트
│   ├── Header.tsx
│   ├── Footer.tsx
│   └── Message.tsx
├── lib/                   # 유틸리티 및 API 클라이언트
│   ├── api.ts            # API 호출 함수
│   └── webauthn.ts      # WebAuthn 유틸리티
├── types/                # TypeScript 타입 정의
│   └── index.ts
└── public/               # 정적 파일
```

## 주요 기능

- 사용자 등록 및 로그인
- WebAuthn/Passkey 등록 및 인증
- **모바일 생체 인증 지원** (지문/얼굴 인식)
- 패스키 관리 (조회, 삭제)
- 반응형 디자인

### 모바일 생체 인증

시스템은 자동으로 디바이스를 감지하여 모바일과 PC에서 다른 인증 방식을 제공합니다:

**모바일 디바이스 (Android/iOS)**
- 기기 내장 생체 인증기 사용 (`authenticatorAttachment: "platform"`)
- 생체 인증 필수 (`userVerification: "required"`)
- 지문 또는 얼굴 인식으로 로그인/등록

**PC 디바이스**
- 기존 WebAuthn 방식 유지
- USB 보안 키 또는 기타 외부 인증기 지원

**지원 환경:**
- Windows Hello (얼굴/지문) → Chrome, Edge, Firefox
- macOS Touch ID → Safari, Chrome
- Android 지문/얼굴 → Chrome
- iOS Face ID/Touch ID → Safari

## 백엔드 연동

백엔드 API는 `http://localhost:8080`에서 실행되어야 합니다. 
`next.config.js`의 `rewrites` 설정을 통해 API 요청을 프록시합니다.

## 환경 변수

`.env.local` 파일을 생성하여 다음 변수를 설정할 수 있습니다:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 주의사항

### 생체 인증 사용 시

1. **HTTPS 필수**: localhost 외에는 반드시 HTTPS 환경이어야 합니다
2. **기기 의존**: 사용자 기기에 생체 인증 하드웨어가 있어야 합니다
3. **Fallback 필요**: 생체 인증 미지원 환경을 위한 대체 로그인 수단(비밀번호) 준비
4. **브라우저 지원**: 최신 브라우저 사용 필요 (Chrome, Safari, Edge, Firefox)

### 디바이스 감지

시스템은 다음 방법으로 모바일 디바이스를 감지합니다:
- User Agent 분석
- 터치 스크린 지원 여부
- 화면 크기 (768px 이하)

