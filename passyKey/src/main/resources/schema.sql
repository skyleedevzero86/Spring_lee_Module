-- users: Spring Security UserDetails 인터페이스 호환
-- password는 BCrypt 해시 저장 (최대 60자 + salt 고려하여 255자)
-- user_handle은 WebAuthn 표준에 따른 Base64 URL 인코딩 값
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    user_handle VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_user_handle (user_handle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- webauthn_credentials: Passkey 저장소
-- credential_id는 Base64 URL 인코딩 (최대 500자 여유)
-- public_key_cose는 COSE 형식 공개키 (최대 2000자)
-- counter는 리플레이 공격 방지용 단조 증가 값
-- CASCADE DELETE로 사용자 삭제 시 자동 정리
CREATE TABLE IF NOT EXISTS webauthn_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id VARCHAR(500) NOT NULL UNIQUE,
    public_key_cose VARCHAR(2000) NOT NULL,
    counter BIGINT NOT NULL DEFAULT 0,
    transports VARCHAR(255) NOT NULL,
    label VARCHAR(100),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_credential_id (credential_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- v_users: 비밀번호 제외한 사용자 정보 조회용 뷰
-- API 응답에서 민감 정보 제외 목적
CREATE OR REPLACE VIEW v_users AS
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

-- v_webauthn_credentials: 인증서와 사용자 정보 조인 뷰
-- 인증서 목록 조회 시 JOIN 쿼리 중복 제거
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
    u.username,
    u.email,
    u.display_name
FROM webauthn_credentials c
INNER JOIN users u ON c.user_id = u.id;
