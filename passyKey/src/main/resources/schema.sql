-- ============================================
-- PassyKey Database Schema
-- WebAuthn/Passkey 기반 인증 시스템의 데이터베이스 스키마
-- 
-- 설계 원칙:
-- 1. Spring Security UserDetails 인터페이스와 호환되는 컬럼명 사용
-- 2. 외래키 CASCADE DELETE로 데이터 정합성 보장
-- 3. 인덱스는 조회 패턴과 유니크 제약조건을 고려하여 설계
-- ============================================

-- 기존 객체 삭제 (의존성 순서 고려: 뷰 -> 테이블)
-- 주의: 프로시저는 procedures.sql에서 별도 관리
DROP VIEW IF EXISTS v_webauthn_credentials;
DROP VIEW IF EXISTS v_users;
DROP TABLE IF EXISTS webauthn_credentials;
DROP TABLE IF EXISTS users;

-- ============================================
-- users 테이블
-- Spring Security UserDetails 인터페이스를 구현하는 사용자 정보 저장
-- 
-- 주요 설계 결정:
-- - account_non_* 패턴: Spring Security의 네이밍 컨벤션 준수
-- - user_handle: WebAuthn 표준에 따른 사용자 식별자 (Base64 URL 인코딩)
-- - password: BCrypt 해시 저장 (255자 충분)
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인 ID, 유니크 제약조건으로 중복 방지',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시된 비밀번호 (최대 60자 + salt 고려하여 255자 할당)',
    email VARCHAR(100) NOT NULL COMMENT '이메일 주소, 향후 이메일 인증 기능 확장 고려',
    display_name VARCHAR(100) NOT NULL COMMENT '화면에 표시될 사용자 이름',
    user_handle VARCHAR(255) NOT NULL COMMENT 'WebAuthn 표준 사용자 핸들 (Base64 URL 인코딩, 유니크하지 않음)',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 여부, Spring Security와 연동',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 만료 여부 (false면 만료)',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 잠금 여부 (false면 잠금)',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '인증서 만료 여부 (false면 만료)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 시각, 감사(audit) 목적',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 시각, 자동 갱신',
    INDEX idx_username (username) COMMENT '로그인 시 username 조회 최적화',
    INDEX idx_email (email) COMMENT '이메일 중복 확인 및 조회 최적화',
    INDEX idx_user_handle (user_handle) COMMENT 'WebAuthn 인증 시 user_handle 조회 최적화'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='사용자 계정 정보 테이블, Spring Security UserDetails 인터페이스 구현';

-- ============================================
-- webauthn_credentials 테이블
-- WebAuthn 인증서(Passkey) 정보 저장
-- 
-- 주요 설계 결정:
-- - credential_id: WebAuthn 표준에 따른 인증서 고유 ID (Base64 URL 인코딩, 500자 여유)
-- - public_key_cose: COSE 형식의 공개키 (2000자 충분)
-- - counter: 리플레이 공격 방지를 위한 서명 카운터 (단조 증가)
-- - CASCADE DELETE: 사용자 삭제 시 연관 인증서 자동 삭제
-- ============================================
CREATE TABLE webauthn_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id VARCHAR(500) NOT NULL UNIQUE COMMENT 'WebAuthn 인증서 고유 ID (Base64 URL 인코딩), 유니크 제약조건',
    public_key_cose VARCHAR(2000) NOT NULL COMMENT 'COSE 형식 공개키 (Base64 URL 인코딩), 서명 검증에 사용',
    counter BIGINT NOT NULL DEFAULT 0 COMMENT '서명 카운터, 리플레이 공격 방지 (단조 증가만 허용)',
    transports VARCHAR(255) NOT NULL COMMENT '지원하는 전송 방식 (usb,nfc,ble,internal 등 쉼표 구분)',
    label VARCHAR(100) COMMENT '사용자가 지정한 인증서 별칭 (예: "내 노트북", "내 스마트폰")',
    user_id BIGINT NOT NULL COMMENT '소유자 사용자 ID, 외래키',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '인증서 등록 시각',
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '최종 사용 시각, 인증 성공 시 갱신',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE COMMENT '사용자 삭제 시 인증서 자동 삭제',
    INDEX idx_credential_id (credential_id) COMMENT '인증 시 credential_id 조회 최적화 (UNIQUE 인덱스)',
    INDEX idx_user_id (user_id) COMMENT '사용자별 인증서 목록 조회 최적화'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='WebAuthn 인증서 정보 테이블, Passkey 저장소';

-- ============================================
-- v_users 뷰
-- 사용자 정보 조회용 뷰 (비밀번호 제외)
-- 
-- 사용 목적:
-- - API 응답에서 민감 정보(password) 제외
-- - 읽기 전용 조회 쿼리 단순화
-- ============================================
CREATE VIEW v_users AS
SELECT 
    id,
    username,
    email,
    display_name,
    user_handle,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    created_at,
    updated_at
FROM users;

-- ============================================
-- v_webauthn_credentials 뷰
-- 인증서와 사용자 정보 조인 뷰
-- 
-- 사용 목적:
-- - 인증서 목록 조회 시 사용자 정보 함께 반환
-- - JOIN 쿼리 중복 제거
-- ============================================
CREATE VIEW v_webauthn_credentials AS
SELECT 
    c.id,
    c.credential_id,
    c.public_key_cose,
    c.counter,
    c.transports,
    c.label,
    c.user_id,
    c.created_at,
    c.last_used_at,
    u.username,
    u.email,
    u.display_name
FROM webauthn_credentials c
INNER JOIN users u ON c.user_id = u.id;

-- ============================================
-- 저장 프로시저는 별도 파일(procedures.sql)로 분리되었습니다.
-- Spring Boot의 ScriptUtils는 DELIMITER를 제대로 처리하지 않으므로
-- 프로시저는 MySQL 클라이언트에서 직접 실행해야 합니다.
-- ============================================
