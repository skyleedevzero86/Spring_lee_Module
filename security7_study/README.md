# Spring Security 7 Study Project

Spring Security 7의 주요 변경사항을 학습하기 위한 프로젝트입니다.

## 프로젝트 개요

이 프로젝트는 Spring Security 7의 새로운 기능과 변경사항을 학습하고 실습하기 위해 설계되었습니다.
**사용자 정의 아키텍처**를 기반으로 구성되었습니다.

## 기술 스택

- **Spring Boot**: 4.0.0
- **Spring Security**: 7.x
- **Java**: 21
- **Thymeleaf**: 템플릿 엔진
- **MyBatis**: 3.0.3
- **MySQL**: 9.1.0
- **JUnit 5**: 테스트 프레임워크

## 프로젝트 아키텍처

이 프로젝트는 **사용자 정의 아키텍처**를 따르며, 다음과 같은 구조로 설계되었습니다:

```
src/main/java/com/sleekydz86/sever/
├── global/                          # 환경 설정 및 유틸리티 (Root)
│   ├── config/
│   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   └── DataInitializer.java     # 초기 데이터 설정
│   └── security/
│       └── CustomUserDetailsService.java  # Spring Security UserDetailsService
│
├── model/                           # 일반 클래스 및 비즈니스 로직 (Root)
│   ├── domain/                      # 도메인 모델
│   │   └── User.java                # 사용자 도메인 모델
│   ├── application/                 # 애플리케이션 서비스
│   │   └── service/
│   │       └── UserService.java     # 사용자 애플리케이션 서비스
│   ├── infrastructure/               # 인프라스트럭처
│   │   └── persistence/
│   │       └── UserMapper.java      # MyBatis Mapper 인터페이스
│   └── presentation/                # 프레젠테이션
│       └── controller/
│           ├── HomeController.java  # 홈 컨트롤러
│           └── LoginController.java # 로그인 컨트롤러
│
└── Security7StudyApplication.java   # 메인 애플리케이션
```

### 아키텍처 설계 원칙

#### 1. **global** (Root)

- **역할**: 환경 설정 및 유틸리티
- **구성**:
  - `config/`: Spring 설정 클래스 (SecurityConfig, DataInitializer)
  - `security/`: Spring Security 관련 구현체

#### 2. **model** (Root)

- **역할**: 일반 클래스 및 비즈니스 로직 통합
- **구성**:
  - `domain/`: 도메인 모델 (비즈니스 엔티티)
  - `application/`: 애플리케이션 서비스 (비즈니스 로직)
  - `infrastructure/`: 인프라스트럭처 (영속성, 외부 연동)
  - `presentation/`: 프레젠테이션 (컨트롤러, 뷰)

## Spring Security 7 주요 변경사항

### 1. `and()` 메소드 제거

- **변경 전**: `.csrf().disable().and().authorizeRequests()...`
- **변경 후**: 람다 체이닝 방식 사용

```java
http
    .csrf(csrf -> csrf.disable())
    .authorizeHttpRequests(auth -> auth...)
```

**이유**: 람다 체이닝이 더 명확하고 타입 안전성을 제공합니다.

### 2. `authorizeRequests()` 제거

- **변경 전**: `authorizeRequests()`
- **변경 후**: `authorizeHttpRequests()` 사용 (Security 6부터 권장)

```java
// Security 6 이전
.authorizeRequests()
    .requestMatchers("/public/**").permitAll()
    .anyRequest().authenticated()

// Security 7
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/public/**").permitAll()
    .anyRequest().authenticated())
```

**이유**: `authorizeHttpRequests()`는 더 명확한 API와 성능 개선을 제공합니다.

### 3. SPA CSRF 설정

- SPA(React, Vue 등) 프로젝트를 위한 `csrf.spa()` 옵션 추가

```java
http.csrf(csrf -> csrf.spa());
```

**기능**: SPA 애플리케이션에서 CSRF 토큰을 자동으로 처리합니다.

### 4. `hasAllRoles()` 신규 메소드

- 모든 권한을 만족해야 하는 AND 조건
- 기존 `hasAnyRole()`은 OR 조건

```java
// OR 조건: USER 또는 ADMIN 중 하나만 있으면 접근 가능
.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

// AND 조건: USER와 VERIFIED 모두 있어야 접근 가능 (Security 7 신규)
.requestMatchers("/special/**").hasAllRoles("USER", "VERIFIED")
```

### 5. `AuthorizationManager.authorize()` 사용

- `check()` 메소드 제거
- `authorize()` 메소드로 거부 이유를 포함할 수 있어 디버깅에 유리

```java
// Security 6 이전
@Override
public AuthorizationDecision check(Supplier<Authentication> authentication, T object) {
    // 허가/거부만 반환
}

// Security 7
@Override
public AuthorizationResult authorize(Supplier<Authentication> authentication, T object) {
    // 거부 이유까지 포함 가능
    throw new AuthorizationDeniedException("Access denied: reason", decision);
}
```

### 6. Password4j 기반 인코더

- Argon2, BCrypt, SCrypt, PBKDF2, Balloon Hashing 지원
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()` 사용 시 자동 지원

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

### 7. 설정 모듈화 지원

- `Customizer<HttpSecurity>` Bean으로 설정을 분리할 수 있습니다.

```java
@Bean
Customizer<HttpSecurity> csrfCustomizer() {
    return http -> http.csrf(csrf -> csrf.disable());
}

@Bean
Customizer<HttpSecurity> formLoginCustomizer() {
    return http -> http.formLogin(withDefaults());
}
```

여러 Customizer Bean을 등록하면 Security가 자동으로 합쳐서 적용합니다.

## 데이터베이스 설정

### 1. MySQL 데이터베이스 생성

```sql
CREATE DATABASE security7_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 테이블 생성

`src/main/resources/schema.sql` 파일을 실행하여 테이블을 생성합니다.

### 3. 초기 데이터

애플리케이션 시작 시 `DataInitializer`가 자동으로 초기 사용자를 생성합니다.

## 테스트 계정

- **일반 사용자**: `user` / `password` (ROLE_USER)
- **관리자**: `admin` / `admin` (ROLE_ADMIN, ROLE_USER)

## 실행 방법

### 1. 데이터베이스 설정

- MySQL 서버 실행
- `security7_db` 데이터베이스 생성
- `src/main/resources/schema.sql` 실행

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 IDE에서 `Security7StudyApplication` 실행

### 3. 접속

- URL: http://localhost:8080
- 로그인 페이지: http://localhost:8080/login

## 주요 엔드포인트

| 엔드포인트              | 설명             | 권한            |
| ---------------------- | ---------------- | --------------- |
| `/`                     | 홈 리다이렉트    | 공개            |
| `/login`                | 로그인 페이지    | 공개            |
| `/users/register`       | 회원가입 페이지  | 공개            |
| `/home`                 | 홈 페이지        | 인증 필요       |
| `/user`                 | 사용자 페이지    | USER 또는 ADMIN |
| `/users/profile`        | 내 정보 조회     | USER 또는 ADMIN |
| `/users/edit`           | 내 정보 수정     | USER 또는 ADMIN |
| `/admin`                | 관리자 페이지    | ADMIN만         |
| `/admin/**`             | 관리자 전용      | ADMIN만         |
| `/users/list`           | 회원 목록        | ADMIN만         |
| `/users/detail/{id}`    | 회원 상세보기    | ADMIN만         |
| `/access-denied`        | 접근 거부 페이지 | 공개            |

## Spring Security 설정 예시

### SecurityConfig 주요 설정

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/public/**", "/css/**", 
                                "/js/**", "/users/register").permitAll()
                .requestMatchers("/admin/**", "/users/list", "/users/detail/**")
                    .hasRole("ADMIN")
                .requestMatchers("/home", "/user/**", "/users/**").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll())
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false))
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

### CustomUserDetailsService

DB 기반 사용자 인증을 위한 `UserDetailsService` 구현:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Collection<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}
```

## 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests SecurityConfigTest

# 테스트 리포트 확인
./gradlew test --info
```

### 테스트 커버리지

테스트는 다음 영역을 포함합니다:

- **SecurityConfig 테스트**: Spring Security 7 설정 검증
  - PasswordEncoder Bean 검증
  - 공개/보호된 엔드포인트 접근 제어 검증
  - CSRF, 세션 관리, 예외 처리 설정 검증

- **컨트롤러 테스트**: 각 엔드포인트의 동작 검증
  - HomeController: 홈, 사용자, 관리자 페이지
  - LoginController: 로그인 페이지 및 파라미터 처리
  - UserController: 회원가입, 프로필, 관리자 기능

- **서비스 테스트**: 비즈니스 로직 검증
  - UserService: 사용자 등록, 조회, 수정, 삭제
  - CustomUserDetailsService: 사용자 인증 정보 로드

- **통합 테스트**: 전체 플로우 검증
  - SecurityIntegrationTest: 보안 설정 통합 검증
  - LoginIntegrationTest: 로그인 프로세스 및 비밀번호 암호화 검증
  - UserIntegrationTest: 사용자 관리 통합 검증

### 테스트 구조

테스트 코드는 프로덕션 코드와 동일한 패키지 구조를 따릅니다:

```
src/test/java/com/sleekydz86/sever/
├── global/
│   ├── config/
│   │   └── SecurityConfigTest.java      # SecurityConfig 설정 테스트
│   └── security/
│       ├── CustomUserDetailsServiceTest.java
│       └── PasswordEncryptionTest.java
├── model/
│   ├── domain/
│   │   └── UserTest.java
│   ├── application/service/
│   │   └── UserServiceTest.java
│   └── presentation/controller/
│       ├── HomeControllerTest.java
│       ├── LoginControllerTest.java
│       └── UserControllerTest.java
└── integration/
    ├── SecurityIntegrationTest.java
    ├── LoginIntegrationTest.java
    └── UserIntegrationTest.java
```

### Spring Security 테스트

Spring Security Test를 사용한 인증/인가 테스트 예시:

#### 1. SecurityConfig 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PasswordEncoder - DelegatingPasswordEncoder 사용")
    void testPasswordEncoder_DelegatingPasswordEncoder() {
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.getClass().getSimpleName()
                  .contains("DelegatingPasswordEncoder"));
    }

    @Test
    @DisplayName("공개 엔드포인트 접근 - /users/register")
    void testPublicEndpoint_Register() throws Exception {
        mockMvc.perform(get("/users/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 전용 엔드포인트 - /users/list (ADMIN 권한 필요)")
    @WithMockUser(username = "user", roles = {"USER"})
    void testAdminEndpoint_UserList_WithUserRole() throws Exception {
        mockMvc.perform(get("/users/list"))
                .andExpect(status().isForbidden());
    }
}
```

#### 2. 컨트롤러 테스트

```java
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminPage_WithAdminRole() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAdminPage_WithUserRole_AccessDenied() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }
}
```

#### 3. 통합 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("로그인 처리 - PasswordEncoder로 비밀번호 검증")
    void testLogin_WithPasswordEncoder() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }
}
```

## 프로젝트 특징

1. **Spring Security 7 반영**: 최신 Security 7 변경사항 적용
2. **DB 기반 인증**: MyBatis를 통한 사용자 인증 구현

## 참고 자료

- [Spring Security 7 Release Notes](https://github.com/spring-projects/spring-security/releases)
- [Spring Security 7 What's New](https://docs.spring.io/spring-security/reference/whats-new.html)
- [개발을 하면서 Spring Security 7 변경후 느낀점](https://velog.io/@sleekydevzero86/spring-security-7-lambda-mfa)
- [Spring Security Reference Documentation](https://docs.spring.io/spring-security/reference/index.html)

## 라이선스

이 프로젝트는 학습 목적으로 작성되었습니다.
