package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.ImageCompareUseCase;
import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(
        name = "Image API",
        description = """
        이미지 업로드 및 OCR 처리 API
        
        주요 기능:
        - 이미지 업로드 및 자동 OCR 처리
        - 얼굴 인식 및 신분증 검증
        - 문서 타입 자동 감지 (영수증, 신분증, 계약서 등)
        - 이미지 간 비교 (얼굴 유사도, OCR 텍스트 유사도)
        
        지원 파일 형식:
        - JPEG, PNG, BMP, GIF 등 이미지 파일
        - 최대 50MB
        """
)
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageUploadUseCase imageUploadUseCase;
    private final ImageCompareUseCase imageCompareUseCase;

    @Operation(
            summary = "이미지 업로드 및 OCR 처리",
            description = """
            이미지 파일을 업로드하고 다음 작업을 자동으로 수행합니다:
            
            1. MinIO 스토리지에 파일 저장
            2. Tesseract OCR로 텍스트 추출 (한국어 + 영어 지원)
            3. DJL을 사용한 얼굴 인식
            4. 문서 타입 자동 감지 (영수증, 신분증, 운전면허증, 여권, 세금계산서, 계약서, 증명서)
            5. 신분증인 경우 정보 추출 및 검증
            6. 민감 정보 암호화 저장 (AES-256-GCM)
            
            처리 결과:
            - OCR 추출 텍스트
            - 얼굴 감지 여부 및 인코딩
            - 문서 타입
            - 신분증 정보 (해당 시)
            
            예시 요청:
            ```
            POST /api/images/upload
            Content-Type: multipart/form-data
            file: [이미지 파일]
            ```
            
            예시 응답:
            ```json
            {
              "success": true,
              "message": "파일 업로드 및 처리 완료",
              "data": {
                "imageId": 1,
                "filename": "receipt.jpg",
                "ocrText": "영수증\\n판매일: 2024-01-15\\n금액: 4,500원",
                "hasFace": false,
                "isIdCard": false,
                "extractedInfo": null
              }
            }
            ```
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 이미지 파일",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "업로드 및 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (파일이 비어있음, 지원하지 않는 형식 등)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 (OCR 처리 실패, 저장소 연결 실패 등)"
            )
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @Parameter(
                    description = "업로드할 이미지 파일 (JPEG, PNG, BMP, GIF, 최대 50MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "파일이 비어있습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            ImageUploadUseCase.UploadResult result = imageUploadUseCase.uploadAndProcess(file);

            response.put("success", true);
            response.put("message", "파일 업로드 및 처리 완료");
            response.put("data", Map.of(
                    "imageId", result.imageId(),
                    "filename", result.filename(),
                    "ocrText", result.ocrText() != null ? result.ocrText() : "",
                    "hasFace", result.hasFace(),
                    "isIdCard", result.isIdCard(),
                    "extractedInfo", result.extractedInfo() != null ? result.extractedInfo() : ""
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "처리 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "최근 이미지와 비교",
            description = """
            현재 이미지를 가장 최근에 업로드된 이미지와 비교합니다.
            
            비교 항목:
            - 얼굴 유사도: 얼굴 인코딩 벡터 간 코사인 유사도 (0-1)
            - OCR 텍스트 유사도: Jaccard 유사도 기반 (0-1)
            
            비교 우선순위:
            1. 얼굴이 둘 다 있는 경우: 얼굴 유사도 사용
            2. 얼굴이 없는 경우: OCR 텍스트 유사도 사용
            
            응답:
            - similarityScore: 최종 유사도 점수 (0-1)
            - comparisonType: 비교 유형 (FACE_SIMILARITY, OCR_TEXT_SIMILARITY, VISUAL_SIMILARITY)
            - details: 상세 비교 정보
            
            예시 응답:
            ```json
            {
              "success": true,
              "data": {
                "currentImageId": 5,
                "previousImageId": 4,
                "similarityScore": 0.87,
                "comparisonType": "FACE_SIMILARITY",
                "details": "Face similarity: 0.87, Text similarity: 0.65"
              }
            }
            ```
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비교 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이미지를 찾을 수 없음"
            )
    })
    @GetMapping("/{imageId}/compare/latest")
    public ResponseEntity<Map<String, Object>> compareWithLatest(
            @Parameter(
                    description = "비교할 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ImageComparison comparison = imageCompareUseCase.compareWithLatest(imageId);

            response.put("success", true);
            response.put("data", Map.of(
                    "currentImageId", comparison.getCurrentImageId(),
                    "previousImageId", comparison.getPreviousImageId(),
                    "similarityScore", comparison.getSimilarityScore(),
                    "comparisonType", comparison.getComparisonType().name(),
                    "details", comparison.getDetails()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비교 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
            summary = "두 이미지 직접 비교",
            description = """
            지정된 두 이미지를 직접 비교합니다.
            
            비교 방식:
            - 얼굴 인코딩이 둘 다 있는 경우: 얼굴 유사도 계산
            - OCR 텍스트가 둘 다 있는 경우: 텍스트 유사도 계산
            - 둘 다 있는 경우: 얼굴 유사도 우선 사용
            
            유사도 점수 해석:
            - 0.7 이상: 매우 유사 (같은 인물/문서일 가능성 높음)
            - 0.4-0.7: 유사 (관련있을 수 있음)
            - 0.4 미만: 유사하지 않음
            
            사용 사례:
            - 신분증 재발급 시 본인 확인
            - 계약서 비교 검증
            - 영수증 중복 검사
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비교 완료"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이미지 중 하나 이상을 찾을 수 없음"
            )
    })
    @GetMapping("/{imageId1}/compare/{imageId2}")
    public ResponseEntity<Map<String, Object>> compareImages(
            @Parameter(
                    description = "첫 번째 이미지 ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long imageId1,
            @Parameter(
                    description = "두 번째 이미지 ID",
                    required = true,
                    example = "2"
            )
            @PathVariable Long imageId2) {

        Map<String, Object> response = new HashMap<>();

        try {
            ImageComparison comparison = imageCompareUseCase.compareImages(imageId1, imageId2);

            response.put("success", true);
            response.put("data", Map.of(
                    "currentImageId", comparison.getCurrentImageId(),
                    "previousImageId", comparison.getPreviousImageId(),
                    "similarityScore", comparison.getSimilarityScore(),
                    "comparisonType", comparison.getComparisonType().name(),
                    "details", comparison.getDetails()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비교 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Schema(description = "이미지 업로드 응답")
    static class UploadResponse {
        @Schema(description = "성공 여부", example = "true")
        public Boolean success;

        @Schema(description = "응답 메시지", example = "파일 업로드 및 처리 완료")
        public String message;

        @Schema(description = "업로드 결과 데이터")
        public UploadData data;
    }

    @Schema(description = "업로드 결과 데이터")
    static class UploadData {
        @Schema(description = "이미지 ID", example = "1")
        public Long imageId;

        @Schema(description = "파일명", example = "receipt.jpg")
        public String filename;

        @Schema(description = "OCR 추출 텍스트", example = "영수증\n판매일: 2024-01-15\n금액: 4,500원")
        public String ocrText;

        @Schema(description = "얼굴 감지 여부", example = "false")
        public Boolean hasFace;

        @Schema(description = "신분증 여부", example = "false")
        public Boolean isIdCard;

        @Schema(description = "추출된 신분증 정보", example = "이름: 홍길동\n주민번호: 900101-1234567")
        public String extractedInfo;
    }
}

