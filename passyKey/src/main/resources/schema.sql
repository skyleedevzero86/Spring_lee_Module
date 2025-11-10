-- ============================================================================
-- PassyKey Database Schema
-- Created Date: 2025-11-10
-- Description: WebAuthn 기반 패스키 인증 시스템 데이터베이스 스키마
-- ============================================================================

-- ============================================================================
-- 사용자 테이블 (users)
-- 설명: 시스템 사용자 정보를 저장하는 테이블
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 식별자',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '로그인에 사용되는 사용자명 (중복 불가)',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호 (BCrypt)',
    email VARCHAR(100) NOT NULL COMMENT '사용자 이메일 주소',
    display_name VARCHAR(100) NOT NULL COMMENT '화면에 표시되는 사용자 이름',
    user_handle VARCHAR(255) NOT NULL COMMENT 'WebAuthn 인증에 사용되는 사용자 핸들 (Base64 URL 인코딩)',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 활성화 여부',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 만료 여부',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '계정 잠금 여부',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '인증 정보 만료 여부',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_handle (user_handle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='시스템 사용자 정보 테이블';

-- ============================================================================
-- WebAuthn 인증서 테이블 (webauthn_credentials)
-- 설명: 사용자가 등록한 WebAuthn 패스키 인증서 정보를 저장하는 테이블
-- ============================================================================
CREATE TABLE IF NOT EXISTS webauthn_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '인증서 고유 식별자',
    credential_id VARCHAR(500) NOT NULL UNIQUE COMMENT 'WebAuthn 인증서 ID (Base64 URL 인코딩, 중복 불가)',
    public_key_cose VARCHAR(2000) NOT NULL COMMENT '공개키 COSE 형식 데이터 (Base64 URL 인코딩)',
    counter BIGINT NOT NULL COMMENT '인증서 사용 횟수 카운터 (리플레이 공격 방지)',
    transports VARCHAR(255) NOT NULL COMMENT '인증서 전송 방식 (usb, nfc, ble, internal 등, 쉼표로 구분)',
    label VARCHAR(100) COMMENT '사용자가 지정한 인증서 이름/라벨',
    user_id BIGINT NOT NULL COMMENT '소유자 사용자 ID (users.id 참조)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증서 등록 일시',
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '인증서 마지막 사용 일시',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_credential_id (credential_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='WebAuthn 패스키 인증서 정보 테이블';

-- ============================================================================
-- 사용자 조회 뷰 (v_users)
-- 설명: 사용자 정보 조회를 위한 뷰 (복수건 조회 최적화)
-- ============================================================================
CREATE OR REPLACE VIEW v_users AS
SELECT 
    id,
    username,
    password,
    email,
    display_name,
    user_handle,
    enabled,
    account_non_expired,
    account_non_locked,
    credentials_non_expired
FROM users;

-- ============================================================================
-- WebAuthn 인증서 조회 뷰 (v_webauthn_credentials)
-- 설명: 인증서와 사용자 정보를 조인하여 조회하는 뷰 (복수건 조회 최적화)
-- ============================================================================
CREATE OR REPLACE VIEW v_webauthn_credentials AS
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
    u.id as user_id,
    u.username,
    u.password,
    u.email,
    u.display_name,
    u.user_handle,
    u.enabled,
    u.account_non_expired,
    u.account_non_locked,
    u.credentials_non_expired
FROM webauthn_credentials c
INNER JOIN users u ON c.user_id = u.id;

-- ============================================================================
-- 사용자 저장 프로시저 (sp_save_user)
-- 설명: 사용자 정보 INSERT/UPDATE를 통합 처리하는 프로시저
-- 파라미터:
--   p_operation: 'C' = INSERT, 'U' = UPDATE
--   p_result_id: 생성/수정된 사용자 ID (OUT 파라미터)
-- ============================================================================
DROP PROCEDURE IF EXISTS sp_save_user;

CREATE PROCEDURE sp_save_user(
    IN p_operation CHAR(1),
    IN p_id BIGINT,
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(255),
    IN p_email VARCHAR(100),
    IN p_display_name VARCHAR(100),
    IN p_user_handle VARCHAR(255),
    IN p_enabled BOOLEAN,
    IN p_account_non_expired BOOLEAN,
    IN p_account_non_locked BOOLEAN,
    IN p_credentials_non_expired BOOLEAN,
    OUT p_result_id BIGINT
)
BEGIN
    IF p_operation = 'C' THEN
        INSERT INTO users (username, password, email, display_name, user_handle, 
                          enabled, account_non_expired, account_non_locked, credentials_non_expired)
        VALUES (p_username, p_password, p_email, p_display_name, p_user_handle,
                p_enabled, p_account_non_expired, p_account_non_locked, p_credentials_non_expired);
        SET p_result_id = LAST_INSERT_ID();
    ELSEIF p_operation = 'U' THEN
        UPDATE users
        SET username = p_username,
            password = p_password,
            email = p_email,
            display_name = p_display_name,
            user_handle = p_user_handle,
            enabled = p_enabled,
            account_non_expired = p_account_non_expired,
            account_non_locked = p_account_non_locked,
            credentials_non_expired = p_credentials_non_expired
        WHERE id = p_id;
        SET p_result_id = p_id;
    END IF;
END;

-- ============================================================================
-- WebAuthn 인증서 저장 프로시저 (sp_save_webauthn_credential)
-- 설명: WebAuthn 인증서 정보 INSERT/UPDATE를 통합 처리하는 프로시저
-- 파라미터:
--   p_operation: 'C' = INSERT, 'U' = UPDATE
--   p_result_id: 생성/수정된 인증서 ID (OUT 파라미터)
-- ============================================================================
DROP PROCEDURE IF EXISTS sp_save_webauthn_credential;

CREATE PROCEDURE sp_save_webauthn_credential(
    IN p_operation CHAR(1),
    IN p_id BIGINT,
    IN p_credential_id VARCHAR(500),
    IN p_public_key_cose VARCHAR(2000),
    IN p_counter BIGINT,
    IN p_transports VARCHAR(255),
    IN p_label VARCHAR(100),
    IN p_user_id BIGINT,
    IN p_created_at TIMESTAMP,
    IN p_last_used_at TIMESTAMP,
    OUT p_result_id BIGINT
)
BEGIN
    IF p_operation = 'C' THEN
        INSERT INTO webauthn_credentials (credential_id, public_key_cose, counter, transports, 
                                         label, user_id, created_at, last_used_at)
        VALUES (p_credential_id, p_public_key_cose, p_counter, p_transports,
                p_label, p_user_id, 
                IFNULL(p_created_at, CURRENT_TIMESTAMP),
                IFNULL(p_last_used_at, CURRENT_TIMESTAMP));
        SET p_result_id = LAST_INSERT_ID();
    ELSEIF p_operation = 'U' THEN
        UPDATE webauthn_credentials
        SET public_key_cose = p_public_key_cose,
            counter = p_counter,
            transports = p_transports,
            label = p_label,
            last_used_at = IFNULL(p_last_used_at, CURRENT_TIMESTAMP)
        WHERE id = p_id;
        SET p_result_id = p_id;
    END IF;
END;
