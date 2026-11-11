-- =====================================================================================
-- 프로시저 정의: 파일 정보 등록 · 수정 · 삭제
-- 프로젝트: Storage Migration (storage_migration)
-- DB: Oracle
-- 작성일: 2026-11-11
-- 설명: 스토리지에 올린 파일의 정보(버킷, 키, 이름, 크기, 체크섬, 제목, 태그)를 SM_STORED_FILE에 저장하고 작업 로그를 남긴다
-- =====================================================================================

-- =====================================================================================
-- 1. 파일 정보 등록 · 수정 · 삭제 (SP_SM_STORED_FILE_CUD)
-- 설명: 작업 구분 C는 등록, U는 수정, D는 삭제한다. 파일 내용은 SP_SM_STORED_FILE_CONTENT_U가 따로 저장한다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE SP_SM_STORED_FILE_CUD(
  in_operation IN VARCHAR2,                                            -- 작업 구분 (C 등록, U 수정, D 삭제)
  in_worker IN VARCHAR2,                                               -- 작업자
  in_workip IN VARCHAR2,                                               -- 작업 IP
  in_id IN VARCHAR2,                                                   -- 파일 ID (UUID)
  in_storage_server_id IN VARCHAR2,                                    -- 스토리지 서버 ID
  in_bucket IN VARCHAR2,                                               -- 버킷 이름
  in_object_key IN VARCHAR2,                                           -- 스토리지 객체 키
  in_original_name IN VARCHAR2,                                        -- 원본 파일 이름
  in_content_type IN VARCHAR2,                                         -- 파일 형식
  in_file_size IN NUMBER,                                              -- 파일 크기 (바이트)
  in_checksum IN VARCHAR2,                                             -- SHA-256 체크섬
  in_title IN VARCHAR2,                                                -- 제목
  in_description IN VARCHAR2,                                          -- 설명
  in_tags IN VARCHAR2,                                                 -- 태그 (쉼표로 구분)
  in_status IN VARCHAR2,                                               -- 상태 (UPLOADED 스토리지만, SAVED DB 저장됨)
  in_uploaded_at IN TIMESTAMP,                                         -- 스토리지 업로드 일시
  in_saved_at IN TIMESTAMP,                                            -- DB 저장 일시
  rtn_cod IN OUT VARCHAR2,                                             -- 반환 코드 (1 성공, 0 실패)
  err_msg IN OUT VARCHAR2                                              -- 실패 사유 (한국어)
) AS
BEGIN
  -- 반환값 초기화
  rtn_cod := '0';
  err_msg := '';

  -- 공통 입력값 확인
  IF TRIM(in_operation) IS NULL THEN err_msg := '작업 구분(operation)이 비어 있습니다.'; RETURN; END IF;
  IF TRIM(in_id) IS NULL THEN err_msg := 'ID가 비어 있습니다.'; RETURN; END IF;

  IF in_operation = 'C' THEN
    -- 등록
    INSERT INTO SM_STORED_FILE (ID, STORAGE_SERVER_ID, BUCKET, OBJECT_KEY, ORIGINAL_NAME, CONTENT_TYPE, FILE_SIZE,
      CHECKSUM, TITLE, DESCRIPTION, TAGS, STATUS, UPLOADED_AT, SAVED_AT, UPDATED_AT, WORKER, WORK_IP, WORK_DT)
    VALUES (in_id, in_storage_server_id, in_bucket, in_object_key, in_original_name, in_content_type, in_file_size,
      in_checksum, in_title, in_description, in_tags, in_status, in_uploaded_at, in_saved_at, SYSTIMESTAMP,
      in_worker, in_workip, SYSTIMESTAMP);
  ELSIF in_operation = 'U' THEN
    -- 수정 (제목 · 설명 · 태그 · 상태 등, 대상이 없으면 실패)
    UPDATE SM_STORED_FILE
       SET TITLE = in_title, DESCRIPTION = in_description, TAGS = in_tags, STATUS = in_status,
           CONTENT_TYPE = in_content_type, CHECKSUM = in_checksum, SAVED_AT = in_saved_at,
           UPDATED_AT = SYSTIMESTAMP, WORKER = in_worker, WORK_IP = in_workip, WORK_DT = SYSTIMESTAMP
     WHERE ID = in_id;
    IF SQL%ROWCOUNT = 0 THEN err_msg := '파일 정보를 찾을 수 없습니다.'; RETURN; END IF;
  ELSIF in_operation = 'D' THEN
    -- 삭제 (대상이 없으면 실패)
    DELETE FROM SM_STORED_FILE WHERE ID = in_id;
    IF SQL%ROWCOUNT = 0 THEN err_msg := '파일 정보를 찾을 수 없습니다.'; RETURN; END IF;
  ELSE
    -- 알 수 없는 작업 구분
    err_msg := '작업 구분은 C, U, D 중 하나여야 합니다.';
    RETURN;
  END IF;

  -- 작업 로그 저장 (STORED_FILE_C, STORED_FILE_U, STORED_FILE_D)
  INSERT INTO SM_AUDIT_LOG (ACTION, TARGET_TYPE, TARGET_ID, DETAIL, WORKER, WORK_IP, WORK_DT)
  VALUES ('STORED_FILE_' || in_operation, 'STORED_FILE', in_id, SUBSTR(in_original_name, 1, 1000), in_worker, in_workip, SYSTIMESTAMP);
  rtn_cod := '1';
EXCEPTION
  -- DB 오류는 예외로 던지지 않고 반환 코드로 돌려준다
  WHEN OTHERS THEN
    rtn_cod := '0';
    err_msg := SUBSTR('DB 오류: ' || SQLERRM, 1, 1000);
END;
