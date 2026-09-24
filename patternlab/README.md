# Primitive Pattern Lab

Java 27 primitive type pattern으로 숫자 값의 정확 변환 매칭을 확인하는 랩입니다.

Spring Boot 4.1.1, Vue 3 + TypeScript + pnpm을 사용합니다.

## 구조

```
number/    숫자 분석 유스케이스 · 도메인 · 어댑터
global/    switch primitive pattern 분석기 · 설정 · 예외
frontend/  Vue + TypeScript
```

## 실행

```powershell
$env:JAVA_HOME = "C:\Users\sleek\.jdks\ms-25.0.2"
.\gradlew.bat bootRun
```

```powershell
cd frontend
pnpm install
pnpm approve-builds
pnpm dev
```

브라우저: http://localhost:5173  
API: `POST /api/patterns/analyze`
