package com.sleekydz86.storagemigration.storage.adapter.in.web;

import com.sleekydz86.storagemigration.global.response.ApiResponse;
import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.dto.RegisterStorageServerCommand;
import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;
import com.sleekydz86.storagemigration.storage.application.dto.UpdateStorageServerCommand;
import com.sleekydz86.storagemigration.storage.application.port.in.RegisterStorageServerUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.StorageServerQueryUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.TestStorageServerUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.UpdateStorageServerUseCase;
import jakarta.validation.Valid;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.openapi.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스토리지 서버")
@RestController
@RequestMapping("/api/storage-servers")
public class StorageServerController {

    private final RegisterStorageServerUseCase register;
    private final UpdateStorageServerUseCase update;
    private final TestStorageServerUseCase test;
    private final StorageServerQueryUseCase query;

    public StorageServerController(RegisterStorageServerUseCase register, UpdateStorageServerUseCase update,
                                   TestStorageServerUseCase test, StorageServerQueryUseCase query) {
        this.register = register;
        this.update = update;
        this.test = test;
        this.query = query;
    }

    @Operation(summary = "스토리지 서버 등록 (SP_SM_STORAGE_SERVER_CUD C)",
            description = "MinIO 콘솔(9001)·MinIO S3 API(9000)·AWS S3 접속 정보를 SM_STORAGE_SERVER에 저장합니다. 아이디·비밀번호는 암호화해 저장하고 작업 로그에 STORAGE_SERVER_C가 남습니다. 등록 직후 상태는 UNTESTED입니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.CRYPTO_FAILED, ErrorCode.DB_OPERATION_FAILED})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StorageServerResult> create(@Valid @RequestBody StorageServerRequest request, Actor actor) {
        return ApiResponse.ok(register.register(new RegisterStorageServerCommand(request.name(), request.type(),
                request.endpoint(), request.region(), request.bucket(), request.accessKey(), request.secretKey()), actor));
    }

    @Operation(summary = "스토리지 서버 목록", description = "사용 중인 DB에 등록된 스토리지 서버를 돌려줍니다. 아이디는 가린 값(maskedAccessKey)만 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED})
    @GetMapping
    public ApiResponse<List<StorageServerResult>> list() {
        return ApiResponse.ok(query.list());
    }

    @Operation(summary = "스토리지 서버 단건 조회", description = "ID로 스토리지 서버 정보 하나를 돌려줍니다. 아이디는 가린 값만 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND})
    @GetMapping("/{id}")
    public ApiResponse<StorageServerResult> get(@Parameter(description = "스토리지 서버 ID (UUID)") @PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "스토리지 서버 수정 (SP_SM_STORAGE_SERVER_CUD U)", description = "접속 정보를 바꿉니다. 아이디·비밀번호를 비우면 기존 값을 유지합니다. 작업 로그에 STORAGE_SERVER_U가 남습니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.CRYPTO_FAILED})
    @PutMapping("/{id}")
    public ApiResponse<StorageServerResult> update(@Parameter(description = "스토리지 서버 ID (UUID)") @PathVariable String id, @Valid @RequestBody StorageServerRequest request,
                                                   Actor actor) {
        return ApiResponse.ok(update.update(new UpdateStorageServerCommand(id, request.name(), request.type(),
                request.endpoint(), request.region(), request.bucket(), request.accessKey(), request.secretKey()), actor));
    }

    @Operation(summary = "스토리지 연결 테스트", description = "로그인과 버킷 확인을 시도하고 결과를 status·lastMessage에 남깁니다. createBucket=true이면 버킷이 없을 때 만듭니다. 연결에 실패해도 200으로 결과를 돌려주며 status가 FAILED가 됩니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.STORAGE_SERVER_NOT_FOUND, ErrorCode.CRYPTO_FAILED})
    @PostMapping("/{id}/test")
    public ApiResponse<StorageServerResult> test(@Parameter(description = "스토리지 서버 ID (UUID)") @PathVariable String id,
                                                 @Parameter(description = "버킷이 없으면 새로 만들지 여부", example = "false") @RequestParam(defaultValue = "false") boolean createBucket,
                                                 Actor actor) {
        return ApiResponse.ok(test.test(id, createBucket, actor));
    }
}
