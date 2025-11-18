-- ============================================
-- PassyKey Database Schema
-- Version: 1.0.0
-- Description: WebAuthn 기반 비밀번호 없는 인증 시스템 데이터베이스 스키마
-- Created: 2025
-- ============================================

-- ============================================
-- 사용자(Users) 테이블
-- ============================================
-- 사용자 계정 정보 및 Spring Security 인증 정보를 저장
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '사용자 고유 식별자',
    
    -- 사용자 식별 정보
    user_id BIGINT UNSIGNED NULL COMMENT '외부 시스템 연동용 사용자 ID (선택적)',
    username VARCHAR(50) NOT NULL COMMENT '사용자명 (로그인 ID)',
    email VARCHAR(100) NOT NULL COMMENT '이메일 주소',
    display_name VARCHAR(100) NOT NULL COMMENT '표시 이름',
    user_handle VARCHAR(255) NOT NULL COMMENT 'WebAuthn 사용자 핸들 (Base64 URL 인코딩)',
    
    -- 인증 정보
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시된 비밀번호 (전통적 로그인용)',
    
    -- 권한 및 상태
    role VARCHAR(50) NULL DEFAULT 'ROLE_USER' COMMENT '사용자 권한 (ROLE_USER, ROLE_ADMIN 등)',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 여부',
    
    -- Spring Security 계정 상태 플래그
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 만료 여부',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 잠금 여부',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '자격 증명 만료 여부',
    
    -- 레거시 호환성 필드 (deprecated, account_non_* 필드 사용 권장)
    account_locked BOOLEAN NOT NULL DEFAULT FALSE COMMENT '계정 잠금 여부 (deprecated)',
    account_expired BOOLEAN NOT NULL DEFAULT FALSE COMMENT '계정 만료 여부 (deprecated)',
    credentials_expired BOOLEAN NOT NULL DEFAULT FALSE COMMENT '자격 증명 만료 여부 (deprecated)',
    
    -- 메타데이터
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '계정 정보 수정 일시',
    
    -- Primary Key 제약조건
    PRIMARY KEY (id),
    
    -- Unique 제약조건
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_user_handle UNIQUE (user_handle),
    
    -- 인덱스
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_user_handle (user_handle),
    INDEX idx_users_user_id (user_id),
    INDEX idx_users_enabled (enabled),
    INDEX idx_users_created_at (created_at)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 계정 정보 테이블';

-- ============================================
-- WebAuthn 인증서(WebAuthn Credentials) 테이블
-- ============================================
-- WebAuthn 공개키 인증서 정보를 저장
-- ============================================
CREATE TABLE IF NOT EXISTS webauthn_credentials (
    -- Primary Key
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '인증서 고유 식별자',
    
    -- 인증서 식별 정보
    credential_id VARCHAR(500) NOT NULL COMMENT 'WebAuthn 인증서 ID (Base64 URL 인코딩)',
    public_key_cose VARCHAR(2000) NOT NULL COMMENT '공개키 COSE 형식 (Base64 URL 인코딩)',
    
    -- 인증서 메타데이터
    counter BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '리플레이 공격 방지를 위한 서명 카운터',
    transports VARCHAR(255) NOT NULL COMMENT '지원하는 전송 방식 (usb,nfc,ble,internal 등 쉼표 구분)',
    label VARCHAR(100) NULL COMMENT '사용자가 지정한 인증서 이름/라벨',
    
    -- 사용자 연관
    user_id BIGINT UNSIGNED NOT NULL COMMENT '소유자 사용자 ID (users.id 참조)',
    
    -- 메타데이터
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증서 등록 일시',
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 사용 일시',
    
    -- Primary Key 제약조건
    PRIMARY KEY (id),
    
    -- Unique 제약조건
    CONSTRAINT uk_webauthn_credentials_credential_id UNIQUE (credential_id),
    
    -- Foreign Key 제약조건
    CONSTRAINT fk_webauthn_credentials_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    
    -- 인덱스
    INDEX idx_webauthn_credentials_credential_id (credential_id(255)),
    INDEX idx_webauthn_credentials_user_id (user_id),
    INDEX idx_webauthn_credentials_created_at (created_at),
    INDEX idx_webauthn_credentials_last_used_at (last_used_at)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci
  COMMENT='WebAuthn 공개키 인증서 정보 테이블';

-- ============================================
-- 인덱스 최적화 힌트
-- ============================================
-- 복합 인덱스는 실제 쿼리 패턴에 따라 추가 고려 필요
-- 예: INDEX idx_users_email_enabled (email, enabled) - 이메일로 활성 사용자 조회 시

-- ============================================
-- 테이블 통계 정보 업데이트
-- ============================================
-- ANALYZE TABLE users;
-- ANALYZE TABLE webauthn_credentials;

-- ============================================
-- 스키마 버전 정보
-- ============================================
-- 현재 스키마 버전: 1.0.0
-- 마지막 수정일: 2025-01-XX
-- 다음 마이그레이션: procedures.sql (저장 프로시저)

