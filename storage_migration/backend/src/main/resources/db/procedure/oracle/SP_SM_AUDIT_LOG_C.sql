-- =====================================================================================
-- 프로시저 정의: 작업 로그 등록
-- 프로젝트: Storage Migration (storage_migration)
-- DB: Oracle
-- 작성일: 2026-11-11
-- 설명: 누가 · 어디서 · 언제 · 무엇을 했는지 SM_AUDIT_LOG에 한 줄 남긴다
-- =====================================================================================

-- =====================================================================================
-- 1. 작업 로그 등록 (SP_SM_AUDIT_LOG_C)
-- 설명: 작업 내용이 비어 있으면 저장하지 않고 실패 사유를 돌려준다
-- =====================================================================================
CREATE OR REPLACE PROCEDURE SP_SM_AUDIT_LOG_C(
  in_worker IN VARCHAR2,                                               -- 작업자
  in_workip IN VARCHAR2,                                               -- 작업 IP
  in_action IN VARCHAR2,                                               -- 작업 내용 (예시 STORED_FILE_C)
  in_target_type IN VARCHAR2,                                          -- 대상 종류 (STORED_FILE, STORAGE_SERVER 등)
  in_target_id IN VARCHAR2,                                            -- 대상 ID
  in_detail IN VARCHAR2,                                               -- 상세 내용 (1000자까지 저장)
  rtn_cod IN OUT VARCHAR2,                                             -- 반환 코드 (1 성공, 0 실패)
  err_msg IN OUT VARCHAR2                                              -- 실패 사유 (한국어)
) AS
BEGIN
  -- 반환값 초기화
  rtn_cod := '0';
  err_msg := '';

  -- 입력값 확인
  IF TRIM(in_action) IS NULL THEN err_msg := '작업 내용(action)이 비어 있습니다.'; RETURN; END IF;

  -- 작업 로그 저장
  INSERT INTO SM_AUDIT_LOG (ACTION, TARGET_TYPE, TARGET_ID, DETAIL, WORKER, WORK_IP, WORK_DT)
  VALUES (in_action, in_target_type, in_target_id, SUBSTR(in_detail, 1, 1000), in_worker, in_workip, SYSTIMESTAMP);
  rtn_cod := '1';
EXCEPTION
  -- DB 오류는 예외로 던지지 않고 반환 코드로 돌려준다
  WHEN OTHERS THEN
    rtn_cod := '0';
    err_msg := SUBSTR('DB 오류: ' || SQLERRM, 1, 1000);
END;
