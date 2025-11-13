package com.sleekydz86.payment2v2.global.exception;

import java.util.List;

public class ValidationException extends BusinessException {
    private final List<String> validationErrors;

    public ValidationException(ErrorCode errorCode, List<String> validationErrors) {
        super(errorCode, String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}

