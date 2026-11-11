package com.sleekydz86.storagemigration.storage.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;
import com.sleekydz86.storagemigration.storage.application.dto.UpdateStorageServerCommand;

public interface UpdateStorageServerUseCase {

    StorageServerResult update(UpdateStorageServerCommand command, Actor actor);
}
