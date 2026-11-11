-- =====================================================================================
-- 프로시저 정의: 파일 정보 등록 · 수정 · 삭제
-- 프로젝트: Storage Migration (storage_migration)
-- DB: MySQL
-- 작성일: 2026-11-11
-- 설명: 스토리지에 올린 파일의 정보(버킷, 키, 이름, 크기, 체크섬, 제목, 태그)를 SM_STORED_FILE에 저장하고 작업 로그를 남긴다
-- =====================================================================================

-- =====================================================================================
-- 1. 파일 정보 등록 · 수정 · 삭제 (SP_SM_STORED_FILE_CUD)
-- 설명: 작업 구분 C는 등록, U는 수정, D는 삭제한다. 파일 내용은 SP_SM_STORED_FILE_CONTENT_U가 따로 저장한다
-- =====================================================================================
CREATE PROCEDURE SP_SM_STORED_FILE_CUD(
  IN in_operation VARCHAR(1),                                          -- 작업 구분 (C 등록, U 수정, D 삭제)
  IN in_worker VARCHAR(100),                                           -- 작업자
  IN in_workip VARCHAR(50),                                            -- 작업 IP
  IN in_id VARCHAR(36),                                                -- 파일 ID (UUID)
  IN in_storage_server_id VARCHAR(36),                                 -- 스토리지 서버 ID
  IN in_bucket VARCHAR(100),                                           -- 버킷 이름
  IN in_object_key VARCHAR(1000),                                      -- 스토리지 객체 키
  IN in_original_name VARCHAR(500),                                    -- 원본 파일 이름
  IN in_content_type VARCHAR(200),                                     -- 파일 형식
  IN in_file_size BIGINT,                                              -- 파일 크기 (바이트)
  IN in_checksum VARCHAR(64),                                          -- SHA-256 체크섬
  IN in_title VARCHAR(200),                                            -- 제목
  IN in_description VARCHAR(2000),                                     -- 설명
  IN in_tags VARCHAR(500),                                             -- 태그 (쉼표로 구분)
  IN in_status VARCHAR(20),                                            -- 상태 (UPLOADED 스토리지만, SAVED DB 저장됨)
  IN in_uploaded_at DATETIME(6),                                       -- 스토리지 업로드 일시
  IN in_saved_at DATETIME(6),                                          -- DB 저장 일시
  INOUT rtn_cod VARCHAR(10),                                           -- 반환 코드 (1 성공, 0 실패)
  INOUT err_msg VARCHAR(1000)                                          -- 실패 사유 (한국어)
)
proc: BEGIN
  -- DB 오류는 예외로 던지지 않고 반환 코드로 돌려준다
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    GET DIAGNOSTICS CONDITION 1 err_msg = MESSAGE_TEXT;
    SET err_msg = LEFT(CONCAT('DB 오류: ', err_msg), 1000);
    SET rtn_cod = '0';
  END;

  -- 반환값 초기화
  SET rtn_cod = '0';
  SET err_msg = '';

  -- 공통 입력값 확인
  IF in_operation IS NULL OR TRIM(in_operation) = '' THEN SET err_msg = '작업 구분(operation)이 비어 있습니다.'; LEAVE proc; END IF;
  IF in_id IS NULL OR TRIM(in_id) = '' THEN SET err_msg = 'ID가 비어 있습니다.'; LEAVE proc; END IF;

  IF in_operation = 'C' THEN
    -- 등록
    INSERT INTO SM_STORED_FILE (ID, STORAGE_SERVER_ID, BUCKET, OBJECT_KEY, ORIGINAL_NAME, CONTENT_TYPE, FILE_SIZE,
      CHECKSUM, TITLE, DESCRIPTION, TAGS, STATUS, UPLOADED_AT, SAVED_AT, UPDATED_AT, WORKER, WORK_IP, WORK_DT)
    VALUES (in_id, in_storage_server_id, in_bucket, in_object_key, in_original_name, in_content_type, in_file_size,
      in_checksum, in_title, in_description, in_tags, in_status, in_uploaded_at, in_saved_at, NOW(6),
      in_worker, in_workip, NOW(6));
  ELSEIF in_operation = 'U' THEN
    -- 수정 (제목 · 설명 · 태그 · 상태 등, 대상이 없으면 실패)
    UPDATE SM_STORED_FILE
       SET TITLE = in_title, DESCRIPTION = in_description, TAGS = in_tags, STATUS = in_status,
           CONTENT_TYPE = in_content_type, CHECKSUM = in_checksum, SAVED_AT = in_saved_at,
           UPDATED_AT = NOW(6), WORKER = in_worker, WORK_IP = in_workip, WORK_DT = NOW(6)
     WHERE ID = in_id;
    IF ROW_COUNT() = 0 THEN SET err_msg = '파일 정보를 찾을 수 없습니다.'; LEAVE proc; END IF;
  ELSEIF in_operation = 'D' THEN
    -- 삭제 (대상이 없으면 실패)
    DELETE FROM SM_STORED_FILE WHERE ID = in_id;
    IF ROW_COUNT() = 0 THEN SET err_msg = '파일 정보를 찾을 수 없습니다.'; LEAVE proc; END IF;
  ELSE
    -- 알 수 없는 작업 구분
    SET err_msg = '작업 구분은 C, U, D 중 하나여야 합니다.';
    LEAVE proc;
  END IF;

  -- 작업 로그 저장 (STORED_FILE_C, STORED_FILE_U, STORED_FILE_D)
  INSERT INTO SM_AUDIT_LOG (ACTION, TARGET_TYPE, TARGET_ID, DETAIL, WORKER, WORK_IP, WORK_DT)
  VALUES (CONCAT('STORED_FILE_', in_operation), 'STORED_FILE', in_id, LEFT(in_original_name, 1000), in_worker, in_workip, NOW(6));
  SET rtn_cod = '1';
END
