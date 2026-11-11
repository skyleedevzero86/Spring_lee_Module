-- =====================================================================================
-- 프로시저 정의: 스토리지 서버 등록 · 수정 · 삭제
-- 프로젝트: Storage Migration (storage_migration)
-- DB: Oracle
-- 작성일: 2026-11-11
-- 설명: MinIO 콘솔, MinIO S3 API, AWS S3 접속 정보를 SM_STORAGE_SERVER에 저장하고 작업 로그를 남긴다
-- =====================================================================================

-- =====================================================================================
-- 1. 스토리지 서버 등록 · 수정 · 삭제 (SP_SM_STORAGE_SERVER_CUD)
-- 설명: 작업 구분 C는 등록, U는 수정, D는 삭제한다. 아이디와 비밀번호는 서버에서 암호화한 값을 받는다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE SP_SM_STORAGE_SERVER_CUD(
  in_operation IN VARCHAR2,                                            -- 작업 구분 (C 등록, U 수정, D 삭제)
  in_worker IN VARCHAR2,                                               -- 작업자
  in_workip IN VARCHAR2,                                               -- 작업 IP
  in_id IN VARCHAR2,                                                   -- 스토리지 서버 ID (UUID)
  in_name IN VARCHAR2,                                                 -- 표시 이름
  in_storage_type IN VARCHAR2,                                         -- 종류 (MINIO_CONSOLE, MINIO, S3)
  in_endpoint IN VARCHAR2,                                             -- 접속 주소
  in_region IN VARCHAR2,                                               -- 리전
  in_bucket IN VARCHAR2,                                               -- 버킷 이름
  in_access_key IN VARCHAR2,                                           -- 아이디 (암호화된 값)
  in_secret_key IN VARCHAR2,                                           -- 비밀번호 (암호화된 값)
  in_status IN VARCHAR2,                                               -- 연결 상태
  in_last_message IN VARCHAR2,                                         -- 마지막 연결 확인 결과
  in_last_checked_at IN TIMESTAMP,                                     -- 마지막 연결 확인 일시
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
    INSERT INTO SM_STORAGE_SERVER (ID, NAME, STORAGE_TYPE, ENDPOINT, REGION, BUCKET, ACCESS_KEY, SECRET_KEY,
      STATUS, LAST_MESSAGE, LAST_CHECKED_AT, CREATED_AT, UPDATED_AT, WORKER, WORK_IP, WORK_DT)
    VALUES (in_id, in_name, in_storage_type, in_endpoint, in_region, in_bucket, in_access_key, in_secret_key,
      in_status, in_last_message, in_last_checked_at, SYSTIMESTAMP, SYSTIMESTAMP, in_worker, in_workip, SYSTIMESTAMP);
  ELSIF in_operation = 'U' THEN
    -- 수정 (대상이 없으면 실패)
    UPDATE SM_STORAGE_SERVER
       SET NAME = in_name, STORAGE_TYPE = in_storage_type, ENDPOINT = in_endpoint, REGION = in_region,
           BUCKET = in_bucket, ACCESS_KEY = in_access_key, SECRET_KEY = in_secret_key, STATUS = in_status,
           LAST_MESSAGE = in_last_message, LAST_CHECKED_AT = in_last_checked_at, UPDATED_AT = SYSTIMESTAMP,
           WORKER = in_worker, WORK_IP = in_workip, WORK_DT = SYSTIMESTAMP
     WHERE ID = in_id;
    IF SQL%ROWCOUNT = 0 THEN err_msg := '스토리지 서버 정보를 찾을 수 없습니다.'; RETURN; END IF;
  ELSIF in_operation = 'D' THEN
    -- 삭제 (대상이 없으면 실패)
    DELETE FROM SM_STORAGE_SERVER WHERE ID = in_id;
    IF SQL%ROWCOUNT = 0 THEN err_msg := '스토리지 서버 정보를 찾을 수 없습니다.'; RETURN; END IF;
  ELSE
    -- 알 수 없는 작업 구분
    err_msg := '작업 구분은 C, U, D 중 하나여야 합니다.';
    RETURN;
  END IF;

  -- 작업 로그 저장 (STORAGE_SERVER_C, STORAGE_SERVER_U, STORAGE_SERVER_D)
  INSERT INTO SM_AUDIT_LOG (ACTION, TARGET_TYPE, TARGET_ID, DETAIL, WORKER, WORK_IP, WORK_DT)
  VALUES ('STORAGE_SERVER_' || in_operation, 'STORAGE_SERVER', in_id, SUBSTR(in_name, 1, 1000), in_worker, in_workip, SYSTIMESTAMP);
  rtn_cod := '1';
EXCEPTION
  -- DB 오류는 예외로 던지지 않고 반환 코드로 돌려준다
  WHEN OTHERS THEN
    rtn_cod := '0';
    err_msg := SUBSTR('DB 오류: ' || SQLERRM, 1, 1000);
END;
