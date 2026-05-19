package com.sleekydz86.monitoring.logstack_s3.interfaces.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public String handleFileNotFound(FileNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.warn("화면 요청 - 파일 없음: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    public String handleBadRequest(RuntimeException ex, RedirectAttributes redirectAttributes) {
        log.warn("화면 요청 - 잘못된 요청: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler({FileStorageException.class, IllegalStateException.class})
    public String handleStorage(RuntimeException ex, RedirectAttributes redirectAttributes) {
        log.error("화면 요청 - 저장 오류: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/upload";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("화면 요청 - 알 수 없는 오류", ex);
        redirectAttributes.addFlashAttribute("error", KoreanMessages.INTERNAL_ERROR);
        return "redirect:/";
    }
}
