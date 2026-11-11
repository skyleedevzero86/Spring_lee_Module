package com.sleekydz86.storagemigration.connection.application.port.in;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import com.sleekydz86.storagemigration.connection.application.dto.RegisterDbConnectionCommand;

public interface RegisterDbConnectionUseCase {

    DbConnectionResult register(RegisterDbConnectionCommand command);
}
