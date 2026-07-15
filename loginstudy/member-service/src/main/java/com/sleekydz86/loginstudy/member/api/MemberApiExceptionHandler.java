package com.sleekydz86.loginstudy.member.api;

import com.sleekydz86.loginstudy.member.service.AccessDeniedBusinessException;
import com.sleekydz86.loginstudy.member.service.OptimisticLockConflictException;
import com.sleekydz86.loginstudy.member.service.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ProblemDetail handleNotFound(ResourceNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("리소스를 찾을 수 없음");
		return problem;
	}

	@ExceptionHandler(AccessDeniedBusinessException.class)
	ProblemDetail handleAccessDenied(AccessDeniedBusinessException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
		problem.setTitle("접근 거부");
		return problem;
	}

	@ExceptionHandler(OptimisticLockConflictException.class)
	ProblemDetail handleOptimisticLock(OptimisticLockConflictException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("낙관적 잠금 충돌");
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다");
		problem.setTitle("잘못된 요청");
		problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList());
		return problem;
	}
}
