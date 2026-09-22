# JVM Dashboard

Java 27 런타임 정보를 보여주는 대시보드입니다. Spring Boot 4.1.1, 헥사고날 구조, Vue 3입니다.
<br/>
조회 기능은 `system` 패키지에 있고, CORS 같은 환경 설정과 화면 포맷 유틸은 `global` 패키지에 있습니다.
<br/>

## 실행

Gradle은 아직 Java 27 위에서 동작하지 않습니다. <br/>
래퍼는 설치된 JDK 17–26으로 띄우고, 컴파일과 `bootRun`은 Azul Zulu 27 툴체인을 받습니다. <br/>
Temurin 27은 아직 배포 목록에 없습니다.<br/>

```powershell
$env:JAVA_HOME = "C:\Users\sleek\.jdks\ms-25.0.2"
.\gradlew.bat bootRun
```

다른 터미널에서 UI를 띄웁니다.

```powershell
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:5173 을 엽니다.<br/>
API는 `GET /api/system` 입니다.<br/>
