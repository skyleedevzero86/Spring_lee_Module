package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.file.application.dto.SaveResult;
import com.sleekydz86.storagemigration.file.application.dto.UpdateFileDetailCommand;
import com.sleekydz86.storagemigration.global.web.Actor;

public interface SaveFileToDatabaseUseCase {

    SaveResult save(UpdateFileDetailCommand detail, Actor actor);
}
