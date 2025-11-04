package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.DocumentAnalysisUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;
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
        name = "Document Analysis API",
        description = "AI 기반 문서 분석, 진위 검증, 문서 요약 기능을 제공하는 API입니다. Spring AI를 활용하여 문서의 내용을 분석하고 검증합니다."
)
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class DocumentAnalysisController {

    private final DocumentAnalysisUseCase documentAnalysisUseCase;

    @Operation(
            summary = "문서 분석",
            description = """
            업로드된 이미지의 OCR 텍스트를 기반으로 AI가 문서를 분석합니다.
            
            분석 내용:
            - 문서 요약 (3-5문장)
            - 진위 여부 점수 (0-1)
            - 감지된 문제점
            - 추출된 주요 필드
            - AI 인사이트
            
            예시:
            - 영수증: 판매 금액, 거래 시간, 판매자 정보 분석
            - 신분증: 기본 정보 확인 및 형식 검증
            - 계약서: 주요 조항 및 당사자 정보 추출
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentAnalysisResponse.class)
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
    @GetMapping("/{imageId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDocument(
            @Parameter(
                    description = "분석할 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId) {

        Map<String, Object> response = new HashMap<>();

        try {
            DocumentAnalysis analysis = documentAnalysisUseCase.analyzeDocument(imageId);

            response.put("success", true);
            response.put("message", "문서 분석이 완료되었습니다.");
            response.put("data", Map.of(
                    "imageId", analysis.getImageId(),
                    "summary", analysis.getSummary() != null ? analysis.getSummary() : "",
                    "authenticityScore", analysis.getAuthenticityScore(),
                    "isAuthentic", analysis.getIsAuthentic(),
                    "detectedIssues", analysis.getDetectedIssues() != null ? analysis.getDetectedIssues() : java.util.List.of(),
                    "extractedFields", analysis.getExtractedFields() != null ? analysis.getExtractedFields() : java.util.Map.of(),
                    "aiInsights", analysis.getAiInsights() != null ? analysis.getAiInsights() : "",
                    "confidence", analysis.getConfidence()
            ));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "이미지를 찾을 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "분석 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "문서 진위 검증",
            description = """
            AI를 활용하여 문서의 진위를 검증합니다.
            
            검증 항목:
            - OCR 텍스트와 추출된 정보의 일관성
            - 문서 형식의 정확성
            - 의심스러운 부분 발견 여부
            
            검증 결과:
            - 진위 여부 (true/false)
            - 검증 점수 (0-1)
            - 발견된 문제점 목록
            
            예시 응답:
            {
              "success": true,
              "data": {
                "isAuthentic": true,
                "authenticityScore": 0.95,
                "detectedIssues": []
              }
            }
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "검증 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이미지를 찾을 수 없음"
            )
    })
    @GetMapping("/{imageId}/verify")
    public ResponseEntity<Map<String, Object>> verifyDocument(
            @Parameter(
                    description = "검증할 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId) {

        Map<String, Object> response = new HashMap<>();

        try {
            DocumentAnalysis verification = documentAnalysisUseCase.verifyDocument(imageId);

            response.put("success", true);
            response.put("message", "문서 검증이 완료되었습니다.");
            response.put("data", Map.of(
                    "imageId", verification.getImageId(),
                    "isAuthentic", verification.getIsAuthentic(),
                    "authenticityScore", verification.getAuthenticityScore(),
                    "detectedIssues", verification.getDetectedIssues() != null ? verification.getDetectedIssues() : java.util.List.of(),
                    "aiInsights", verification.getAiInsights() != null ? verification.getAiInsights() : "",
                    "confidence", verification.getConfidence()
            ));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "이미지를 찾을 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "검증 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "문서 요약",
            description = """
            OCR로 추출한 텍스트를 기반으로 AI가 문서의 주요 내용을 요약합니다.
            
            요약 특징:
            - 3-5문장으로 간결하게 요약
            - 중요한 정보(날짜, 금액, 이름 등) 포함
            - 문서 종류에 맞는 형식으로 작성
            
            예시:
            - 영수증: "2024년 1월 15일 오후 2시에 스타벅스 강남점에서 아메리카노를 구매하여 4,500원을 결제했습니다."
            - 신분증: "홍길동의 주민등록증으로, 1990년 1월 1일생이며 서울시 강남구에 거주합니다."
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "요약 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이미지를 찾을 수 없음"
            )
    })
    @GetMapping("/{imageId}/summarize")
    public ResponseEntity<Map<String, Object>> summarizeDocument(
            @Parameter(
                    description = "요약할 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId) {

        Map<String, Object> response = new HashMap<>();

        try {
            DocumentAnalysis summary = documentAnalysisUseCase.summarizeDocument(imageId);

            response.put("success", true);
            response.put("message", "문서 요약이 완료되었습니다.");
            response.put("data", Map.of(
                    "imageId", summary.getImageId(),
                    "summary", summary.getSummary() != null ? summary.getSummary() : "",
                    "confidence", summary.getConfidence()
            ));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "이미지를 찾을 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "요약 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Schema(description = "문서 분석 응답")
    static class DocumentAnalysisResponse {
        @Schema(description = "성공 여부", example = "true")
        public Boolean success;

        @Schema(description = "응답 메시지", example = "문서 분석이 완료되었습니다.")
        public String message;

        @Schema(description = "분석 결과 데이터")
        public DocumentAnalysisData data;
    }

    @Schema(description = "문서 분석 데이터")
    static class DocumentAnalysisData {
        @Schema(description = "이미지 ID", example = "1")
        public Long imageId;

        @Schema(description = "문서 요약", example = "2024년 1월 15일 스타벅스에서 4,500원을 결제한 영수증입니다.")
        public String summary;

        @Schema(description = "진위 여부 점수 (0-1)", example = "0.95")
        public Double authenticityScore;

        @Schema(description = "진위 여부", example = "true")
        public Boolean isAuthentic;

        @Schema(description = "감지된 문제점 목록", example = "[]")
        public java.util.List<String> detectedIssues;

        @Schema(description = "추출된 필드 정보", example = "{\"금액\": \"4500\", \"날짜\": \"2024-01-15\"}")
        public java.util.Map<String, Object> extractedFields;

        @Schema(description = "AI 인사이트", example = "정상적인 문서로 판단됩니다.")
        public String aiInsights;

        @Schema(description = "분석 신뢰도", example = "0.85")
        public Double confidence;
    }
}

