package com.sleekydz86.monitoring.logstack_s3.domain.repository;

import java.util.Optional;

import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;

public interface FileRepository {

    StoredFile save(StoredFile file);

    StoredFile update(StoredFile file);

    void delete(String id);

    Optional<StoredFile> findById(String id);

    PageResult<StoredFileSummary> search(Optional<String> keyword, int page, int size);

    void seedDemoData(int count);
}
