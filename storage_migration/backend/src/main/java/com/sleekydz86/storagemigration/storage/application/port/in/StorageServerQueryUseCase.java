package com.sleekydz86.storagemigration.storage.application.port.in;

import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;
import java.util.List;

public interface StorageServerQueryUseCase {

    StorageServerResult get(String id);

    List<StorageServerResult> list();
}
