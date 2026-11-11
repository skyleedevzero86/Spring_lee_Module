package com.sleekydz86.storagemigration.connection.application.port.in;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import com.sleekydz86.storagemigration.connection.application.dto.UpdateDbConnectionCommand;

public interface UpdateDbConnectionUseCase {

    DbConnectionResult update(UpdateDbConnectionCommand command);
}
