package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.application.dto.UploadFileCommand;
import com.sleekydz86.storagemigration.global.web.Actor;

public interface UploadFileUseCase {

    StoredFileResult upload(UploadFileCommand command, Actor actor);
}
