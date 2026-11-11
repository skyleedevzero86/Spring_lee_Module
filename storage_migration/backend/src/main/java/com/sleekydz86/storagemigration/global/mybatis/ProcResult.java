package com.sleekydz86.storagemigration.global.mybatis;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;

public abstract class ProcResult {

    public static final String SUCCESS = "1";

    private String rtnCod = "0";
    private String errMsg = "";

    public String getRtnCod() {
        return rtnCod;
    }

    public void setRtnCod(String rtnCod) {
        this.rtnCod = rtnCod;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public void verify(String procedureName) {
        if (!SUCCESS.equals(rtnCod)) {
            String message = errMsg == null || errMsg.isBlank() ? "rtn_cod=" + rtnCod : errMsg;
            throw new BusinessException(ErrorCode.DB_OPERATION_FAILED, procedureName + ": " + message);
        }
    }
}
