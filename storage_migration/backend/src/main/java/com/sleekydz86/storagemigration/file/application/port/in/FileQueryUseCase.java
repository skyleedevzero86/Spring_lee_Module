package com.sleekydz86.storagemigration.file.application.port.in;

import com.sleekydz86.storagemigration.file.application.dto.StorageFileView;
import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.domain.FileContent;
import com.sleekydz86.storagemigration.file.domain.FileSearchCondition;
import java.util.List;

public interface FileQueryUseCase {

    StoredFileResult get(String id);

    List<StoredFileResult> search(FileSearchCondition condition);

    List<StorageFileView> browseStorage(String storageServerId, String prefix);

    FileContent openContent(String id);

    FileContent openDatabaseContent(String id);
}
