# FTP 트러블슈팅 서버

DDD(Domain-Driven Design) 기반으로 구현된 FTP 전송 및 트러블슈팅 시스템입니다.

## 주요 기능

### 1. 비동기 FTP 전송
- **트랜잭션 분리**: FTP 전송을 비동기로 처리하여 DB 연결을 오래 점유하지 않음
- **지출결의번호 보존**: FTP 전송 실패 시에도 지출결의번호는 유지됨
- **상태 추적**: 전송 상태를 실시간으로 추적 가능

### 2. 자동 재시도 메커니즘
- **스마트 재시도**: 실패한 전송을 자동으로 재시도 (최대 3회)
- **스케줄러 기반**: 1분마다 실패한 전송을 자동으로 재시도
- **수동 재시도**: API를 통해 특정 전송을 수동으로 재시도 가능

### 3. 트러블슈팅 기능
- **연결 테스트**: FTP 서버 연결 상태 확인
- **통계 조회**: 전송 성공률, 실패 건수 등 통계 제공
- **이벤트 로깅**: 모든 전송 이벤트를 기록하여 추적 가능

### 4. 동시성 처리
- **스레드 풀 관리**: 연결 풀 설정에 맞춰 스레드 풀 구성
- **동시 전송 지원**: 여러 전송을 동시에 처리 가능

## 아키텍처

### DDD 레이어 구조

```
src/main/java/com/sleekydz86/ftpserver/
├── domain/                    # 도메인 레이어
│   └── ftp/
│       ├── FtpTransfer.java          # 도메인 엔티티
│       ├── FtpTransferId.java        # 값 객체
│       ├── FtpTransferStatus.java   # 값 객체 (Enum)
│       ├── FtpTransferEvent.java    # 도메인 이벤트
│       └── FtpTransferRepository.java # 리포지토리 인터페이스
│
├── application/              # 애플리케이션 레이어
│   └── ftp/
│       ├── FtpTransferService.java          # FTP 전송 서비스
│       ├── FtpTroubleshootingService.java   # 트러블슈팅 서비스
│       └── FtpTransferCommand.java         # 커맨드 DTO
│
├── infrastructure/           # 인프라스트럭처 레이어
│   └── ftp/
│       ├── FtpClient.java                  # FTP 클라이언트 구현
│       ├── FtpConnectionProperties.java    # 설정 프로퍼티
│       ├── FtpTransferRepositoryImpl.java  # 리포지토리 구현
│       └── FtpException.java              # 예외 클래스
│
├── presentation/             # 프레젠테이션 레이어
│   └── ftp/
│       └── FtpTransferController.java     # REST API 컨트롤러
│
└── config/                   # 설정
    └── FtpConfig.java        # FTP 관련 설정
```

## 문제 해결

### 문제 1: 네트워크 I/O를 트랜잭션 내에서 실행
**해결책**: 
- `@Async`를 사용하여 FTP 전송을 비동기로 처리
- 트랜잭션을 분리하여 DB 연결을 오래 점유하지 않음

```java
@Async("ftpExecutor")
@Transactional
public void transferAsync(FtpTransferId transferId) {
    // FTP 전송은 트랜잭션 밖에서 실행
}
```

### 문제 2: FTP 서버 응답 지연
**해결책**:
- 타임아웃 설정 (application.yml)
- 연결 풀 관리
- 재시도 메커니즘

### 문제 3: 동시성 문제
**해결책**:
- 스레드 풀 기반 비동기 처리
- 연결 풀 설정에 맞춰 스레드 수 제한

### 문제 4: 예외 처리 부족
**해결책**:
- 자동 재시도 메커니즘
- 상세한 에러 메시지
- 이벤트 기반 로깅

## API 엔드포인트

### 1. FTP 전송 요청
```http
POST /api/ftp/transfer
Content-Type: application/json

{
  "paymentRequestNo": "PAY-2024-001",
  "remotePath": "project/2024",
  "fileName": "document.pdf",
  "fileContent": "base64_encoded_content"
}
```

### 2. 전송 상태 조회
```http
GET /api/ftp/transfer/{transferId}
```

### 3. 지출결의번호로 전송 이력 조회
```http
GET /api/ftp/transfer/payment/{paymentRequestNo}
```

### 4. FTP 전송 재시도
```http
POST /api/ftp/transfer/{transferId}/retry
```

### 5. FTP 연결 테스트
```http
GET /api/ftp/test-connection
```

### 6. 전송 통계 조회
```http
GET /api/ftp/statistics
```

## 설정

### application.yml
```yaml
ftp:
  server:
    host: localhost
    port: 21
    username: testuser
    password: "1234"
    passive-mode:
      enabled: true
      port-range:
        min: 21000
        max: 21010
    timeout:
      connect: 5000
      data: 30000
    debug: false
    pool:
      max-total: 10
      max-idle: 5
      min-idle: 2
```

## 실행 방법

### 1. Docker FTP 서버 실행
```bash
docker run -d --name ftp-server \
  -p 21:21 \
  -p 21000-21010:21000-21010 \
  -e USERS="testuser|1234" \
  -e ADDRESS=localhost \
  delfer/alpine-ftp-server
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

## 사용 예제

### Java 코드에서 사용
```java
@Autowired
private FtpTransferService transferService;

// FTP 전송 요청
FtpTransferId transferId = transferService.requestTransfer(
    "PAY-2024-001",
    "project/2024",
    "document.pdf",
    fileContent
);

// 전송 상태 확인
Optional<FtpTransfer> transfer = transferService.getTransfer(transferId);
if (transfer.isPresent()) {
    System.out.println("상태: " + transfer.get().getStatus());
}
```

## 전송 상태

- `PENDING`: 대기중
- `IN_PROGRESS`: 진행중
- `SUCCESS`: 성공
- `FAILED`: 실패
- `RETRYING`: 재시도중
- `CANCELLED`: 취소됨

## 트러블슈팅

### FTP 연결 실패
1. FTP 서버가 실행 중인지 확인
2. 포트가 열려있는지 확인 (21, 21000-21010)
3. 사용자명/비밀번호 확인
4. `/api/ftp/test-connection`으로 연결 테스트

### 전송 실패
1. `/api/ftp/transfer/{transferId}`로 상태 확인
2. 에러 메시지 확인
3. 필요시 `/api/ftp/transfer/{transferId}/retry`로 재시도

### 통계 확인
- `/api/ftp/statistics`로 전체 통계 확인
- 성공률, 실패 건수 등 확인

## 향후 개선 사항

1. **데이터베이스 연동**: 현재는 인메모리 저장소 사용, 실제 DB 연동 필요
2. **이벤트 발행**: Spring Events를 사용한 이벤트 발행
3. **모니터링**: Micrometer를 사용한 메트릭 수집
4. **알림**: 전송 실패 시 알림 기능
5. **배치 처리**: 대량 전송을 위한 배치 처리 기능
