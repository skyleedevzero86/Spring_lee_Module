package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;
import java.util.List;

public interface DeleteStoredFilesUseCase {

    int delete(List<String> ids, boolean deleteFromStorage, Actor actor);

    int deleteStorageObjects(String storageServerId, List<String> keys, Actor actor);
}
