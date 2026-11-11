package com.sleekydz86.storagemigration.connection.application.port.in;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;

public interface TestDbConnectionUseCase {

    DbConnectionResult test(String id);
}
