package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileListRow;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileProcedureParam;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileRow;

@Mapper
public interface StoredFileMapper {

    void callManage(StoredFileProcedureParam param);

    long selectMaxSequence(@Param("dateTimePrefix") String dateTimePrefix);

    StoredFileRow selectById(@Param("id") String id);

    List<StoredFileListRow> selectPageFromView(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countFromView(@Param("keyword") String keyword);
}
