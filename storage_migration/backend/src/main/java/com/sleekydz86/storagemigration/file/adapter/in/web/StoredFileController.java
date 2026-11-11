package com.sleekydz86.storagemigration.file.adapter.in.web;

import com.sleekydz86.storagemigration.file.application.dto.SaveResult;
import com.sleekydz86.storagemigration.file.application.dto.StorageFileView;
import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.application.dto.UpdateFileDetailCommand;
import com.sleekydz86.storagemigration.file.application.dto.UploadFileCommand;
import com.sleekydz86.storagemigration.file.application.port.in.DeleteStoredFilesUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.FileQueryUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.ImportStorageFilesUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.SaveFileToDatabaseUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.UpdateFileDetailUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.UploadFileUseCase;
import com.sleekydz86.storagemigration.file.domain.FileContent;
import com.sleekydz86.storagemigration.file.domain.FileSearchCondition;
import com.sleekydz86.storagemigration.file.domain.FileStatus;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.response.ApiResponse;
import com.sleekydz86.storagemigration.global.web.Actor;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "파일")
@RestController
@RequestMapping("/api/files")
public class StoredFileController {

    private final UploadFileUseCase upload;
    private final ImportStorageFilesUseCase importFiles;
    private final UpdateFileDetailUseCase updateDetail;
    private final SaveFileToDatabaseUseCase saveToDatabase;
    private final DeleteStoredFilesUseCase deleteFiles;
    private final FileQueryUseCase query;

    public StoredFileController(UploadFileUseCase upload, ImportStorageFilesUseCase importFiles,
                                UpdateFileDetailUseCase updateDetail, SaveFileToDatabaseUseCase saveToDatabase,
                                DeleteStoredFilesUseCase deleteFiles, FileQueryUseCase query) {
        this.upload = upload;
        this.importFiles = importFiles;
        this.updateDetail = updateDetail;
        this.saveToDatabase = saveToDatabase;
        this.deleteFiles = deleteFiles;
        this.query = query;
    }

    @Operation(summary = "파일 업로드 (스토리지 저장 + 파일 정보 등록, 선택 시 BLOB 저장)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<StoredFileResult>> upload(@RequestParam String storageServerId,
                                                      @RequestParam(defaultValue = "false") boolean saveToDb,
                                                      @RequestPart("files") List<MultipartFile> files, Actor actor) {
        List<StoredFileResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new BusinessException(ErrorCode.FILE_EMPTY, file.getOriginalFilename());
            }
            StoredFileResult uploaded;
            try {
                uploaded = upload.upload(new UploadFileCommand(storageServerId, file.getOriginalFilename(),
                        file.getContentType(), file.getSize(), file.getInputStream()), actor);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
            if (saveToDb) {
                uploaded = saveToDatabase.save(new UpdateFileDetailCommand(uploaded.id(), null, null, null), actor).file();
            }
            results.add(uploaded);
        }
        return ApiResponse.ok(results);
    }

    @Operation(summary = "파일 목록·검색")
    @GetMapping
    public ApiResponse<List<StoredFileResult>> search(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String storageServerId,
                                                      @RequestParam(required = false) FileStatus status) {
        return ApiResponse.ok(query.search(new FileSearchCondition(keyword, storageServerId, status)));
    }

    @Operation(summary = "파일 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<StoredFileResult> get(@PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "파일 상세 정보 수정")
    @PutMapping("/{id}")
    public ApiResponse<StoredFileResult> update(@PathVariable String id, @Valid @RequestBody FileDetailRequest request,
                                                Actor actor) {
        return ApiResponse.ok(updateDetail.update(
                new UpdateFileDetailCommand(id, request.title(), request.description(), request.tags()), actor));
    }

    @Operation(summary = "DB에 저장 (상세 정보 + 파일 BLOB)")
    @PostMapping("/{id}/save")
    public ApiResponse<SaveResult> save(@PathVariable String id, @Valid @RequestBody FileDetailRequest request,
                                        Actor actor) {
        return ApiResponse.ok(saveToDatabase.save(
                new UpdateFileDetailCommand(id, request.title(), request.description(), request.tags()), actor));
    }

    @Operation(summary = "파일 정보 삭제 (선택 시 스토리지 파일도 삭제)")
    @DeleteMapping
    public ApiResponse<Map<String, Integer>> delete(@Valid @RequestBody DeleteFilesRequest request, Actor actor) {
        return ApiResponse.ok(Map.of("deleted", deleteFiles.delete(request.ids(), request.deleteFromStorage(), actor)));
    }

    @Operation(summary = "파일 내용 보기·다운로드")
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable String id,
                                                       @RequestParam(defaultValue = "false") boolean download) {
        FileContent content = query.openContent(id);
        MediaType mediaType = content.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(content.contentType());
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(content.fileName(), StandardCharsets.UTF_8)
                .build();
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        if (content.size() > 0) {
            builder.contentLength(content.size());
        }
        return builder.body(new InputStreamResource(content.stream()));
    }

    @Operation(summary = "스토리지 파일 목록")
    @GetMapping("/storage-objects")
    public ApiResponse<List<StorageFileView>> browse(@RequestParam String storageServerId,
                                                     @RequestParam(required = false) String prefix) {
        return ApiResponse.ok(query.browseStorage(storageServerId, prefix));
    }

    @Operation(summary = "스토리지 파일을 DB에 등록")
    @PostMapping("/storage-objects/import")
    public ApiResponse<List<StoredFileResult>> importObjects(@Valid @RequestBody StorageKeysRequest request,
                                                             Actor actor) {
        return ApiResponse.ok(importFiles.importFiles(request.storageServerId(), request.keys(), actor));
    }

    @Operation(summary = "스토리지 파일 삭제 (등록된 DB 정보도 삭제)")
    @DeleteMapping("/storage-objects")
    public ApiResponse<Map<String, Integer>> deleteObjects(@Valid @RequestBody StorageKeysRequest request,
                                                           Actor actor) {
        return ApiResponse.ok(Map.of("deleted",
                deleteFiles.deleteStorageObjects(request.storageServerId(), request.keys(), actor)));
    }
}
