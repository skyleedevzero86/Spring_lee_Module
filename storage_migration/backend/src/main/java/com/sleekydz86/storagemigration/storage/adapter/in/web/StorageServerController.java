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

    @Operation(summary = "스토리지 서버 등록 (SP_SM_STORAGE_SERVER_CUD C)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StorageServerResult> create(@Valid @RequestBody StorageServerRequest request, Actor actor) {
        return ApiResponse.ok(register.register(new RegisterStorageServerCommand(request.name(), request.type(),
                request.endpoint(), request.region(), request.bucket(), request.accessKey(), request.secretKey()), actor));
    }

    @Operation(summary = "스토리지 서버 목록")
    @GetMapping
    public ApiResponse<List<StorageServerResult>> list() {
        return ApiResponse.ok(query.list());
    }

    @Operation(summary = "스토리지 서버 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<StorageServerResult> get(@PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "스토리지 서버 수정 (SP_SM_STORAGE_SERVER_CUD U)")
    @PutMapping("/{id}")
    public ApiResponse<StorageServerResult> update(@PathVariable String id, @Valid @RequestBody StorageServerRequest request,
                                                   Actor actor) {
        return ApiResponse.ok(update.update(new UpdateStorageServerCommand(id, request.name(), request.type(),
                request.endpoint(), request.region(), request.bucket(), request.accessKey(), request.secretKey()), actor));
    }

    @Operation(summary = "스토리지 연결 테스트")
    @PostMapping("/{id}/test")
    public ApiResponse<StorageServerResult> test(@PathVariable String id,
                                                 @RequestParam(defaultValue = "false") boolean createBucket,
                                                 Actor actor) {
        return ApiResponse.ok(test.test(id, createBucket, actor));
    }
}
