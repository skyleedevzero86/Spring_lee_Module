# LoginStudy Identity Platform

회원·인증 스프링4.X 학습 프로젝트입니다.

## 실행

```powershell
.\gradlew.bat :auth-server:bootRun
.\gradlew.bat :member-service:bootRun
.\gradlew.bat :user-portal:bootRun
.\gradlew.bat :admin-portal:bootRun
```

사용자는 `http://localhost:8080`으로만 접속합니다.

<br/>

| Service        | Docker 공개 주소      |
| -------------- | --------------------- |
| Main(root)     | http://localhost:8080 |
| auth-server    | 내부 전용 (`9000`)    |
| user-portal    | 내부 전용 (`8081`)    |
| admin-portal   | 내부 전용 (`8082`)    |
| member-service | 내부 전용 (`8083`)    |
