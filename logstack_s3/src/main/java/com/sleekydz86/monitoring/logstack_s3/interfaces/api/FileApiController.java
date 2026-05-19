package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sleekydz86.monitoring.logstack_s3.application.query.SearchFilesQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.UploadFileCommand;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.DeleteFileUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.GetFileDetailUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.SearchFilesUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.UploadFileUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "파일 API", description = "파일 업로드·목록·상세·삭제")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileApiController {

    private final UploadFileUseCase uploadFileUseCase;
    private final SearchFilesUseCase searchFilesUseCase;
    private final GetFileDetailUseCase getFileDetailUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    @Operation(summary = "파일 업로드", description = "S3에 저장하고 DB에 메타데이터를 등록합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileDetailView> upload(
            @Parameter(description = "업로드할 파일") @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(uploadFileUseCase.apply(new UploadFileCommand(file)));
    }

    @Operation(summary = "파일 목록", description = "뷰 기반 목록 조회(페이징·검색).")
    @GetMapping
    public FilePageResponse list(
            @Parameter(description = "페이지 번호(0부터)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "파일명 검색어") @RequestParam(required = false) String keyword
    ) {
        return FilePageResponse.from(searchFilesUseCase.apply(new SearchFilesQuery(keyword, page, size)));
    }

    @Operation(summary = "파일 상세", description = "미리보기·다운로드 URL을 포함합니다.")
    @GetMapping("/{id}")
    public FileDetailView detail(
            @Parameter(description = "파일 ID (예: lky_20260520_1430_0001)") @PathVariable String id
    ) {
        return getFileDetailUseCase.apply(id);
    }

    @Operation(summary = "파일 삭제", description = "DB 메타데이터를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "파일 ID") @PathVariable String id
    ) {
        deleteFileUseCase.apply(id);
        return ResponseEntity.noContent().build();
    }
}
