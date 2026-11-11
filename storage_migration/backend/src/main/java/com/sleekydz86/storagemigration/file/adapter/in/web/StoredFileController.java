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
import com.sleekydz86.storagemigration.global.openapi.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "파일 업로드 (스토리지 저장 + 파일 정보 등록, 선택 시 BLOB 저장)",
            description = "files를 여러 개 받아 하나씩 스토리지에 올리면서 SHA-256 체크섬을 계산하고, SP_SM_STORED_FILE_CUD(C)로 파일 정보를 등록합니다(status UPLOADED). saveToDb=true이면 이어서 파일 내용을 SM_STORED_FILE.CONTENT에 저장합니다(status SAVED). 요청 크기 상한은 500MB, DB 저장 상한은 max-db-content-bytes(기본 50MB)입니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.FILE_EMPTY, ErrorCode.FILE_TOO_LARGE, ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.STORAGE_OPERATION_FAILED})
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<StoredFileResult>> upload(
            @Parameter(description = "스토리지 서버 ID", required = true) @RequestParam String storageServerId,
            @Parameter(description = "올린 뒤 파일 내용도 DB(BLOB)에 저장할지 여부", example = "true") @RequestParam(defaultValue = "false") boolean saveToDb,
            @Parameter(description = "올릴 파일 (여러 개 가능)") @RequestPart("files") List<MultipartFile> files, Actor actor) {
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

    @Operation(summary = "파일 목록·검색", description = "등록된 파일을 돌려줍니다. 조건을 비우면 전체를 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED})
    @GetMapping
    public ApiResponse<List<StoredFileResult>> search(
            @Parameter(description = "파일명·제목·태그·설명에 포함된 글자 (대소문자 무시)", example = "보고서") @RequestParam(required = false) String keyword,
            @Parameter(description = "스토리지 서버 ID로 좁히기") @RequestParam(required = false) String storageServerId,
            @Parameter(description = "상태로 좁히기 (UPLOADED 스토리지에만 있음, SAVED DB에도 저장됨)") @RequestParam(required = false) FileStatus status) {
        return ApiResponse.ok(query.search(new FileSearchCondition(keyword, storageServerId, status)));
    }

    @Operation(summary = "파일 단건 조회", description = "ID로 파일 정보 하나를 돌려줍니다. 파일 내용은 /content 또는 /db-content로 받습니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND})
    @GetMapping("/{id}")
    public ApiResponse<StoredFileResult> get(@Parameter(description = "파일 ID (UUID)") @PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "파일 상세 정보 수정", description = "제목·설명·태그만 바꿉니다(SP_SM_STORED_FILE_CUD U). 파일 내용은 바뀌지 않습니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND})
    @PutMapping("/{id}")
    public ApiResponse<StoredFileResult> update(@Parameter(description = "파일 ID (UUID)") @PathVariable String id, @Valid @RequestBody FileDetailRequest request,
                                                Actor actor) {
        return ApiResponse.ok(updateDetail.update(
                new UpdateFileDetailCommand(id, request.title(), request.description(), request.tags()), actor));
    }

    @Operation(summary = "DB에 저장 (상세 정보 + 파일 BLOB)", description = "상세 정보를 저장하고, 스토리지 원본을 읽어 SP_SM_STORED_FILE_CONTENT_U로 SM_STORED_FILE.CONTENT에 넣습니다. 넣는 동안 SHA-256을 다시 계산해 업로드 때 체크섬, DB에 들어간 바이트 수와 비교하고 결과를 verified로 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND, ErrorCode.FILE_TOO_LARGE, ErrorCode.STORAGE_OBJECT_NOT_FOUND, ErrorCode.STORAGE_OPERATION_FAILED})
    @PostMapping("/{id}/save")
    public ApiResponse<SaveResult> save(@Parameter(description = "파일 ID (UUID)") @PathVariable String id, @Valid @RequestBody FileDetailRequest request,
                                        Actor actor) {
        return ApiResponse.ok(saveToDatabase.save(
                new UpdateFileDetailCommand(id, request.title(), request.description(), request.tags()), actor));
    }

    @Operation(summary = "파일 정보 삭제 (선택 시 스토리지 파일도 삭제)", description = "ids의 파일 정보를 지웁니다(SP_SM_STORED_FILE_CUD D). deleteFromStorage=true이면 스토리지 파일도 함께 지웁니다. data.deleted에 지운 개수가 옵니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND, ErrorCode.STORAGE_OPERATION_FAILED})
    @DeleteMapping
    public ApiResponse<Map<String, Integer>> delete(@Valid @RequestBody DeleteFilesRequest request, Actor actor) {
        return ApiResponse.ok(Map.of("deleted", deleteFiles.delete(request.ids(), request.deleteFromStorage(), actor)));
    }

    @Operation(summary = "파일 내용 보기·다운로드", description = "스토리지 원본을 내려줍니다. 스토리지에서 읽지 못하고 DB에 저장된 파일이면 SM_STORED_FILE.CONTENT로 대신 내려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND, ErrorCode.STORAGE_OBJECT_NOT_FOUND})
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@Parameter(description = "파일 ID (UUID)") @PathVariable String id,
                                                       @Parameter(description = "true이면 다운로드(attachment), false이면 브라우저에서 바로 보기(inline)", example = "false") @RequestParam(defaultValue = "false") boolean download) {
        return respond(query.openContent(id), download);
    }

    @Operation(summary = "DB에 저장한 파일 내용 보기·다운로드 (SM_STORED_FILE.CONTENT)", description = "스토리지와 관계없이 DB의 CONTENT 컬럼(Oracle BLOB, MySQL LONGBLOB, PostgreSQL BYTEA)만 읽어 내려줍니다. DB에 저장(SAVED)한 파일만 볼 수 있습니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.FILE_NOT_FOUND, ErrorCode.FILE_NOT_SAVED_IN_DB})
    @GetMapping("/{id}/db-content")
    public ResponseEntity<InputStreamResource> databaseContent(@Parameter(description = "파일 ID (UUID)") @PathVariable String id,
                                                               @Parameter(description = "true이면 다운로드(attachment), false이면 브라우저에서 바로 보기(inline)", example = "false") @RequestParam(defaultValue = "false") boolean download) {
        return respond(query.openDatabaseContent(id), download);
    }

    private static ResponseEntity<InputStreamResource> respond(FileContent content, boolean download) {
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

    @Operation(summary = "스토리지 파일 목록", description = "스토리지 버킷의 파일을 그대로 읽어 오고, DB에 등록된 파일이면 storedFileId와 status를 함께 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.STORAGE_CONNECTION_FAILED, ErrorCode.STORAGE_OPERATION_FAILED})
    @GetMapping("/storage-objects")
    public ApiResponse<List<StorageFileView>> browse(
            @Parameter(description = "스토리지 서버 ID", required = true) @RequestParam String storageServerId,
            @Parameter(description = "경로 앞부분으로 좁히기", example = "uploads/2026/") @RequestParam(required = false) String prefix) {
        return ApiResponse.ok(query.browseStorage(storageServerId, prefix));
    }

    @Operation(summary = "스토리지 파일을 DB에 등록", description = "스토리지에만 있는 파일의 정보를 SM_STORED_FILE에 등록합니다(status UPLOADED). 이미 등록된 키는 건너뜁니다. 파일 내용까지 넣으려면 DB에 저장 API를 부르세요.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.STORAGE_OBJECT_NOT_FOUND})
    @PostMapping("/storage-objects/import")
    public ApiResponse<List<StoredFileResult>> importObjects(@Valid @RequestBody StorageKeysRequest request,
                                                             Actor actor) {
        return ApiResponse.ok(importFiles.importFiles(request.storageServerId(), request.keys(), actor));
    }

    @Operation(summary = "스토리지 파일 삭제 (등록된 DB 정보도 삭제)", description = "keys의 스토리지 파일을 지우고, DB에 등록된 정보가 있으면 함께 지웁니다. data.deleted에 지운 개수가 옵니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.STORAGE_OPERATION_FAILED})
    @DeleteMapping("/storage-objects")
    public ApiResponse<Map<String, Integer>> deleteObjects(@Valid @RequestBody StorageKeysRequest request,
                                                           Actor actor) {
        return ApiResponse.ok(Map.of("deleted",
                deleteFiles.deleteStorageObjects(request.storageServerId(), request.keys(), actor)));
    }
}
