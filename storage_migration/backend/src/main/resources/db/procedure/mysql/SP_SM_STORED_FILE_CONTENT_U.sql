-- =====================================================================================
-- 프로시저 정의: 파일 내용 저장
-- 프로젝트: Storage Migration (storage_migration)
-- DB: MySQL
-- 작성일: 2026-11-11
-- 설명: 스토리지에 있는 파일 내용을 SM_STORED_FILE.CONTENT(LONGBLOB)에 저장하고 상태를 SAVED로 바꾼다
-- =====================================================================================

-- =====================================================================================
-- 1. 파일 내용 저장 (SP_SM_STORED_FILE_CONTENT_U)
-- 설명: 파일 정보가 먼저 등록되어 있어야 하며, 저장한 바이트 수를 작업 로그에 남긴다
-- =====================================================================================
CREATE PROCEDURE SP_SM_STORED_FILE_CONTENT_U(
  IN in_worker VARCHAR(100),                                           -- 작업자
  IN in_workip VARCHAR(50),                                            -- 작업 IP
  IN in_id VARCHAR(36),                                                -- 파일 ID (UUID)
  IN in_content LONGBLOB,                                              -- 파일 내용
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

  -- 입력값 확인
  IF in_id IS NULL OR TRIM(in_id) = '' THEN SET err_msg = 'ID가 비어 있습니다.'; LEAVE proc; END IF;

  -- 파일 내용 저장 (대상이 없으면 실패)
  UPDATE SM_STORED_FILE
     SET CONTENT = in_content, STATUS = 'SAVED', SAVED_AT = in_saved_at, UPDATED_AT = NOW(6),
         WORKER = in_worker, WORK_IP = in_workip, WORK_DT = NOW(6)
   WHERE ID = in_id;
  IF ROW_COUNT() = 0 THEN SET err_msg = '파일 정보를 찾을 수 없습니다.'; LEAVE proc; END IF;

  -- 작업 로그 저장 (저장한 바이트 수 기록)
  INSERT INTO SM_AUDIT_LOG (ACTION, TARGET_TYPE, TARGET_ID, DETAIL, WORKER, WORK_IP, WORK_DT)
  VALUES ('STORED_FILE_SAVE', 'STORED_FILE', in_id, CONCAT('bytes=', IFNULL(LENGTH(in_content), 0)), in_worker, in_workip, NOW(6));
  SET rtn_cod = '1';
END
