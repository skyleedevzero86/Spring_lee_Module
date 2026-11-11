package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.application.dto.UpdateFileDetailCommand;
import com.sleekydz86.storagemigration.global.web.Actor;

public interface UpdateFileDetailUseCase {

    StoredFileResult update(UpdateFileDetailCommand command, Actor actor);
}
