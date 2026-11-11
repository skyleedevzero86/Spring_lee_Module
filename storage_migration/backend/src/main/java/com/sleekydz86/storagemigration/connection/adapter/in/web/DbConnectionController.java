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

    @Operation(summary = "DB 연결 등록",
            description = "Oracle·MySQL·PostgreSQL 접속 정보를 등록합니다. 아이디·비밀번호는 AES-GCM으로 암호화해 서버의 db-connections.json에 저장하고, 이 단계에서는 접속하지 않습니다. 포트를 비우면 DB 종류의 기본 포트를 씁니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.CRYPTO_FAILED})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DbConnectionResult> create(@Valid @RequestBody DbConnectionRequest request) {
        return ApiResponse.ok(register.register(new RegisterDbConnectionCommand(request.name(), request.vendor(),
                request.host(), request.port(), request.database(), request.username(), request.password())));
    }

    @Operation(summary = "DB 연결 목록", description = "등록된 모든 DB 접속 정보를 돌려줍니다. 아이디는 가린 값(maskedUsername)만, 비밀번호는 돌려주지 않습니다.")
    @GetMapping
    public ApiResponse<List<DbConnectionResult>> list() {
        return ApiResponse.ok(query.list());
    }

    @Operation(summary = "사용 중인 DB 연결", description = "지금 작업 데이터를 저장하는 DB를 돌려줍니다. 고른 DB가 없으면 data가 null입니다.")
    @GetMapping("/active")
    public ApiResponse<DbConnectionResult> active() {
        return ApiResponse.ok(query.active().orElse(null));
    }

    @Operation(summary = "DB 연결 단건 조회", description = "ID로 DB 접속 정보 하나를 돌려줍니다. 아이디는 가린 값만 돌려줍니다.")
    @ApiErrorCodes({ErrorCode.DB_CONNECTION_NOT_FOUND})
    @GetMapping("/{id}")
    public ApiResponse<DbConnectionResult> get(@Parameter(description = "DB 연결 ID (UUID)") @PathVariable String id) {
        return ApiResponse.ok(query.get(id));
    }

    @Operation(summary = "DB 연결 수정", description = "접속 정보를 바꿉니다. 아이디·비밀번호를 비우면 기존 값을 유지합니다. 바꾼 뒤에는 연결 테스트를 다시 해야 합니다.")
    @ApiErrorCodes({ErrorCode.INVALID_INPUT, ErrorCode.DB_CONNECTION_NOT_FOUND, ErrorCode.CRYPTO_FAILED})
    @PutMapping("/{id}")
    public ApiResponse<DbConnectionResult> update(@Parameter(description = "DB 연결 ID (UUID)") @PathVariable String id, @Valid @RequestBody DbConnectionRequest request) {
        return ApiResponse.ok(update.update(new UpdateDbConnectionCommand(id, request.name(), request.vendor(),
                request.host(), request.port(), request.database(), request.username(), request.password())));
    }

    @Operation(summary = "DB 연결 테스트", description = "DB 종류별 확인 쿼리(Oracle은 SELECT 1 FROM DUAL, 그 밖의 DB는 SELECT 1)를 실행하고 결과를 status·lastMessage·lastCheckedAt에 남깁니다. 연결에 실패해도 200으로 결과를 돌려주며 status가 FAILED가 됩니다.")
    @ApiErrorCodes({ErrorCode.DB_CONNECTION_NOT_FOUND, ErrorCode.CRYPTO_FAILED})
    @PostMapping("/{id}/test")
    public ApiResponse<DbConnectionResult> test(@Parameter(description = "DB 연결 ID (UUID)") @PathVariable String id) {
        return ApiResponse.ok(test.test(id));
    }

    @Operation(summary = "이 DB 사용 (테이블·프로시저 준비)", description = "연결 테스트에 성공한 DB를 작업 데이터 저장소로 고릅니다. 커넥션 풀(HikariCP, 최대 5개)을 열고, SM_STORAGE_SERVER·SM_STORED_FILE·SM_AUDIT_LOG 테이블과 SP_SM_* 프로시저를 없으면 만듭니다. 서버를 다시 켜면 마지막에 고른 DB에 자동으로 다시 연결합니다.")
    @ApiErrorCodes({ErrorCode.DB_CONNECTION_NOT_FOUND, ErrorCode.DB_CONNECTION_NOT_TESTED, ErrorCode.DB_CONNECTION_FAILED, ErrorCode.DB_OPERATION_FAILED})
    @PostMapping("/{id}/activate")
    public ApiResponse<DbConnectionResult> activate(@Parameter(description = "DB 연결 ID (UUID)") @PathVariable String id, Actor actor) {
        return ApiResponse.ok(activate.activate(id, actor));
    }
}
