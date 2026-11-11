package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.global.web.Actor;
import java.util.List;

public interface ImportStorageFilesUseCase {

    List<StoredFileResult> importFiles(String storageServerId, List<String> keys, Actor actor);
}
