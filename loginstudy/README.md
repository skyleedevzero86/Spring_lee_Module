# LoginStudy Identity Platform

회원·인증 스프링4.X 학습 프로젝트입니다.

## 실행

```powershell
.\gradlew.bat :auth-server:bootRun
.\gradlew.bat :member-service:bootRun
.\gradlew.bat :user-portal:bootRun
.\gradlew.bat :admin-portal:bootRun
```

| Service        | Port      |
| -------------- | --------- |
| auth-server    | 9000      |
| user-portal    | 8081      |
| admin-portal   | 8082      |
| member-service | 8083      |
| PostgreSQL     | 5432      |
| Redis Cluster  | 7001-7006 |
