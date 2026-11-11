package com.sleekydz86.storagemigration.storage.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.dto.RegisterStorageServerCommand;
import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;

public interface RegisterStorageServerUseCase {

    StorageServerResult register(RegisterStorageServerCommand command, Actor actor);
}
