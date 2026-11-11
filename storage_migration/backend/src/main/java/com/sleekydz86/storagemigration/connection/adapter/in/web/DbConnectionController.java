package com.sleekydz86.storagemigration.connection.adapter.in.web;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import com.sleekydz86.storagemigration.connection.application.dto.RegisterDbConnectionCommand;
import com.sleekydz86.storagemigration.connection.application.dto.UpdateDbConnectionCommand;
import com.sleekydz86.storagemigration.connection.application.port.in.ActivateDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.DbConnectionQueryUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.RegisterDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.TestDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.UpdateDbConnectionUseCase;
import com.sleekydz86.storagemigration.global.response.ApiResponse;
import com.sleekydz86.storagemigration.global.web.Actor;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DB 연결")
@RestController
@RequestMapping("/api/db-connections")
public class DbConnectionController {

    private final RegisterDbConnectionUseCase register;
    private final UpdateDbConnectionUseCase update;
    private final TestDbConnectionUseCase test;
    private final ActivateDbConnectionUseCase activate;
    private final DbConnectionQueryUseCase query;

    public DbConnectionController(RegisterDbConnectionUseCase register, UpdateDbConnectionUseCase update,
                                  TestDbConnectionUseCase test, ActivateDbConnectionUseCase activate,
                                  DbConnectionQueryUseCase query) {
        this.register = register;
        this.update = update;
        this.test = test;
        this.activate = activate;
        this.query = query;
    }

    @Operation(summary = "DB 연결 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DbConnectionResult> create(@Valid @RequestBody DbConnectionRequest request) {
        return ApiResponse.ok(register.register(new RegisterDbConnectionCommand(request.name(), request.vendor(),
                request.host(), request.port(), request.database(), request.username(), request.password())));
    }

    @Operation(summary = "DB 연결 목록")
    @GetMapping
    public ApiResponse<List<DbConnectionResult>> list() {
        return ApiResponse.ok(query.list());
    }

    @Operation(summary = "사용 중인 DB 연결")
    @GetMapping("/active")
    public ApiResponse<DbConnectionResult> active() {
        return ApiResponse.ok(query.active().orElse(null));
    }

    @Operation(summary = "DB 연결 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<DbConnectionResult> get(@PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "DB 연결 수정")
    @PutMapping("/{id}")
    public ApiResponse<DbConnectionResult> update(@PathVariable String id, @Valid @RequestBody DbConnectionRequest request) {
        return ApiResponse.ok(update.update(new UpdateDbConnectionCommand(id, request.name(), request.vendor(),
                request.host(), request.port(), request.database(), request.username(), request.password())));
    }

    @Operation(summary = "DB 연결 테스트")
    @PostMapping("/{id}/test")
    public ApiResponse<DbConnectionResult> test(@PathVariable String id) {
        return ApiResponse.ok(test.test(id));
    }

    @Operation(summary = "이 DB 사용 (테이블·프로시저 준비)")
    @PostMapping("/{id}/activate")
    public ApiResponse<DbConnectionResult> activate(@PathVariable String id, Actor actor) {
        return ApiResponse.ok(activate.activate(id, actor));
    }
}
