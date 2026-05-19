package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.monitoring.logstack_s3.application.usecase.SeedFilesUseCase;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "관리 API", description = "대용량 데모 데이터 시드")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class BulkSeedController {

    private final SeedFilesUseCase seedFilesUseCase;

    @Operation(summary = "데모 데이터 시드", description = "대용량 목록/페이징 테스트용 데이터를 생성합니다.")
    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed(
            @Parameter(description = "생성 건수 (1~500000)") @RequestParam(defaultValue = "10000") int count
    ) {
        int seeded = seedFilesUseCase.apply(count);
        return ResponseEntity.ok(Map.of(
                "seeded", seeded,
                "message", KoreanMessages.seedComplete(seeded)
        ));
    }
}
