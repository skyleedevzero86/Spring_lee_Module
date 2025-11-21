package com.sleekydz86.tran.domain.adapter.in.web;


import com.sleekydz86.tran.domain.application.dto.TranslationRequestDto;
import com.sleekydz86.tran.domain.application.dto.TranslationResponseDto;
import com.sleekydz86.tran.domain.application.usecase.TranslateTextUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {


    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);

    private final TranslateTextUseCase translateTextUseCase;

    public TranslationController(TranslateTextUseCase translateTextUseCase) {
        this.translateTextUseCase = translateTextUseCase;
    }


    @PostMapping("/translate")
    public ResponseEntity<TranslationResponseDto> translate(
            @Valid @RequestBody TranslationRequestDto requestDto
    ) {
        logger.info("번역 API 호출: {}", requestDto);

        TranslationResponseDto response = translateTextUseCase.translate(requestDto);

        logger.debug("번역 API 응답: {}", response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<ServiceStatusResponse> getStatus() {
        boolean available = translateTextUseCase.isServiceAvailable();
        String usageInfo = translateTextUseCase.getServiceUsageInfo();

        ServiceStatusResponse response = new ServiceStatusResponse(
                available,
                available ? "서비스 정상" : "서비스 사용 불가",
                usageInfo
        );

        return ResponseEntity.ok(response);
    }

    public static class ServiceStatusResponse {
        private final boolean available;
        private final String message;
        private final String usageInfo;

        public ServiceStatusResponse(boolean available, String message, String usageInfo) {
            this.available = available;
            this.message = message;
            this.usageInfo = usageInfo;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getMessage() {
            return message;
        }

        public String getUsageInfo() {
            return usageInfo;
        }
    }
}