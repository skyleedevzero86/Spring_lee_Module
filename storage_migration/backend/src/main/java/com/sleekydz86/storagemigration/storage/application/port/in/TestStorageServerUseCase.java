package com.sleekydz86.storagemigration.storage.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;

public interface TestStorageServerUseCase {

    StorageServerResult test(String id, boolean createBucketIfMissing, Actor actor);
}
