package com.sleekydz86.storagemigration.connection.application.port.in;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import java.util.List;
import java.util.Optional;

public interface DbConnectionQueryUseCase {

    DbConnectionResult get(String id);

    List<DbConnectionResult> list();

    Optional<DbConnectionResult> active();
}
