package com.sleekydz86.storagemigration.storage.application.port.in;

import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.domain.StorageObject;
import com.sleekydz86.storagemigration.storage.domain.StorageObjectContent;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface StorageObjectUseCase {

    String bucketOf(String storageServerId);

    List<StorageObject> list(String storageServerId, String prefix);

    Optional<StorageObject> stat(String storageServerId, String key);

    void put(String storageServerId, String key, InputStream content, long size, String contentType);

    StorageObjectContent open(String storageServerId, String key);

    int delete(String storageServerId, List<String> keys, Actor actor);
}
