package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StorageBucketRow;

@Mapper
public interface StorageBucketMapper {

    List<StorageBucketRow> selectPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit);

    long count(@Param("keyword") String keyword);

    StorageBucketRow selectByBucketCode(@Param("bucketCode") String bucketCode);
}
