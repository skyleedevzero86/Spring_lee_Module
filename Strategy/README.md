# Strategy Pattern CRUD Project

Strategy 디자인 패턴을 적용한 CRUD 시스템입니다.

## 주요 기능

- **Strategy 패턴**: Insert, Update, Delete 작업을 전략으로 분리
- **MVC 패턴**: Controller, Service, Repository 계층 구조
- **MyBatis 연동**: XML Mapper를 통한 데이터베이스 접근
- **Redis 캐싱**: 조회 성능 향상을 위한 캐싱 적용
- **프로시저 사용**: MySQL 저장 프로시저를 통한 CRUD 작업

## 프로젝트 구조

```
src/main/java/com/sleekydz86/strategy/
├── config/              # 설정 클래스
│   ├── MyBatisConfig.java
│   └── RedisConfig.java
├── controller/          # REST API 컨트롤러
│   └── ProductController.java
├── domain/              # 도메인 모델
│   └── Product.java
├── dto/                  # 데이터 전송 객체
│   ├── ProductSearchRequest.java
│   └── ProductSearchResponse.java
├── repository/          # 데이터 접근 계층
│   └── ProductRepository.java
├── service/             # 비즈니스 로직 계층
│   └── ProductService.java
└── strategy/            # Strategy 패턴 구현
    ├── CrudStrategy.java
    ├── StrategyFactory.java
    └── impl/
        ├── InsertStrategy.java
        ├── UpdateStrategy.java
        └── DeleteStrategy.java
```

## 데이터베이스 설정

1. MySQL 데이터베이스 생성:

```sql
CREATE DATABASE strategy_db;
```

2. 테이블, 프로시저 및 뷰 생성:
   - `src/main/resources/sql/schema.sql` 파일 실행
   - 뷰 `v_product_search`는 복잡한 조건 조회를 위해 생성됩니다

## 환경 설정

`application.properties` 파일에서 다음 설정을 수정하세요:

- 데이터베이스 연결 정보
- Redis 연결 정보

## API 엔드포인트

### INSERT (Create)

```http
POST /api/products
Content-Type: application/json

{
  "name": "Product Name",
  "description": "Product Description",
  "price": 100.0,
  "stock": 50
}
```

### UPDATE

```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated Description",
  "price": 150.0,
  "stock": 30
}
```

### DELETE

```http
DELETE /api/products/{id}
```

### SELECT (Read) - 단건 조회 (테이블 조회)

```http
GET /api/products/{id}
```

### SEARCH - 복잡한 조건 조회 (뷰 조회)

```http
GET /api/products/search?name=Product&minPrice=100&maxPrice=500&minStock=10&sortBy=price&sortOrder=DESC&page=0&size=10
```

**쿼리 파라미터:**

- `name`: 상품명 검색 (LIKE 검색)
- `minPrice`: 최소 가격
- `maxPrice`: 최대 가격
- `minStock`: 최소 재고
- `maxStock`: 최대 재고
- `sortBy`: 정렬 기준 (id, name, price, stock, createdAt)
- `sortOrder`: 정렬 방향 (ASC, DESC)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)

### 프로시저 실행

```http
POST /api/products/procedure?operation=INSERT
Content-Type: application/json

{
  "name": "Product Name",
  "description": "Product Description",
  "price": 100.0,
  "stock": 50
}
```

## Strategy 패턴 설명

각 CRUD 작업은 별도의 Strategy 구현체로 분리되어 있습니다:

- **InsertStrategy**: INSERT 작업 처리
- **UpdateStrategy**: UPDATE 작업 처리
- **DeleteStrategy**: DELETE 작업 처리

StrategyFactory를 통해 작업 타입에 맞는 Strategy를 선택하여 실행합니다.

## 조회 전략

### 단건 조회 (테이블 조회)

- `GET /api/products/{id}`: `products` 테이블에서 직접 조회
- 빠른 단건 조회에 최적화
- Redis 캐싱 적용

### 복잡한 조건 조회 (뷰 조회)

- `GET /api/products/search`: `v_product_search` 뷰를 사용한 조회
- 여러 조건 조합, 정렬, 페이징 지원
- 대량 데이터 조회에 최적화
- Redis 캐싱 적용

## 기술 스택

- Spring Boot 3.5.7
- Java 21
- MyBatis 3.0.3
- MySQL
- Redis
- Gradle
