package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.IdVerificationUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.IdVerification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(
        name = "ID Verification API",
        description = """
        신분증 인증 및 정보 추출 API
        
        주요 기능:
        - OCR 텍스트를 기반으로 신분증 여부 자동 감지
        - 신분증 유형 식별 (주민등록증, 운전면허증, 여권)
        - 신분증 정보 자동 추출 (이름, 주민번호, 생년월일, 주소 등)
        - 정규식 패턴 매칭을 통한 정보 검증
        
        지원 문서:
        - 주민등록증 (National ID Card)
        - 운전면허증 (Driver License)
        - 여권 (Passport)
        """
)
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class IdVerificationController {

    private final IdVerificationUseCase idVerificationUseCase;

    @Operation(
            summary = "신분증 인증 및 정보 추출",
            description = """
            업로드된 이미지가 신분증인지 확인하고, 신분증인 경우 자동으로 정보를 추출합니다.
            
            인증 프로세스:
            1. OCR 텍스트에서 신분증 키워드 검색 (주민등록증, 운전면허, 여권 등)
            2. 주민번호 패턴 검증 (000000-0000000 형식)
            3. 문서 유형 자동 분류
            4. 신분증 정보 추출 (이름, 주민번호, 생년월일, 주소 등)
            
            추출 가능한 정보:
            - 이름 (Name)
            - 주민번호 (ID Number)
            - 생년월일 (Date of Birth)
            - 주소 (Address)
            - 만료일 (Expiry Date) - 운전면허증, 여권
            - 발급기관 (Issuing Authority) - 여권
            
            예시 응답:
            ```json
            {
              "success": true,
              "data": {
                "isIdCard": true,
                "documentType": "national_id",
                "extractedInfo": {
                  "name": "홍길동",
                  "idNumber": "900101-1234567",
                  "dateOfBirth": "1990-01-01",
                  "address": "서울시 강남구"
                },
                "verificationConfidence": 0.8
              }
            }
            ```
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VerificationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이미지를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/{imageId}/verify")
    public ResponseEntity<Map<String, Object>> verifyIdCard(
            @Parameter(
                    description = "인증할 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId) {

        Map<String, Object> response = new HashMap<>();

        try {
            IdVerification verification = idVerificationUseCase.verifyIdCard(imageId);

            Map<String, Object> extractedInfoMap = new HashMap<>();
            if (verification.getExtractedInfo() != null) {
                IdVerification.ExtractedInfo info = verification.getExtractedInfo();
                extractedInfoMap.put("name", info.getName());
                extractedInfoMap.put("idNumber", info.getIdNumber());
                extractedInfoMap.put("dateOfBirth", info.getDateOfBirth());
                extractedInfoMap.put("address", info.getAddress());
                extractedInfoMap.put("expiryDate", info.getExpiryDate());
                extractedInfoMap.put("issuingAuthority", info.getIssuingAuthority());
            }

            response.put("success", true);
            response.put("message", "신분증 인증이 완료되었습니다.");
            response.put("data", Map.of(
                    "isIdCard", verification.getIsIdCard(),
                    "documentType", verification.getDocumentType() != null ? verification.getDocumentType() : "unknown",
                    "extractedInfo", extractedInfoMap,
                    "verificationConfidence", verification.getVerificationConfidence()
            ));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "이미지를 찾을 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Schema(description = "신분증 인증 응답")
    static class VerificationResponse {
        @Schema(description = "성공 여부", example = "true")
        public Boolean success;

        @Schema(description = "응답 메시지", example = "신분증 인증이 완료되었습니다.")
        public String message;

        @Schema(description = "인증 결과 데이터")
        public VerificationData data;
    }

    @Schema(description = "인증 결과 데이터")
    static class VerificationData {
        @Schema(description = "신분증 여부", example = "true")
        public Boolean isIdCard;

        @Schema(description = "문서 유형", example = "national_id",
                allowableValues = {"national_id", "driver_license", "passport", "unknown"})
        public String documentType;

        @Schema(description = "추출된 정보")
        public ExtractedInfoData extractedInfo;

        @Schema(description = "인증 신뢰도 (0-1)", example = "0.8")
        public Double verificationConfidence;
    }

    @Schema(description = "추출된 정보")
    static class ExtractedInfoData {
        @Schema(description = "이름", example = "홍길동")
        public String name;

        @Schema(description = "주민번호", example = "900101-1234567")
        public String idNumber;

        @Schema(description = "생년월일", example = "1990-01-01")
        public String dateOfBirth;

        @Schema(description = "주소", example = "서울시 강남구")
        public String address;

        @Schema(description = "만료일", example = "2030-12-31")
        public String expiryDate;

        @Schema(description = "발급기관", example = "서울남부출입국관리사무소")
        public String issuingAuthority;
    }
}

