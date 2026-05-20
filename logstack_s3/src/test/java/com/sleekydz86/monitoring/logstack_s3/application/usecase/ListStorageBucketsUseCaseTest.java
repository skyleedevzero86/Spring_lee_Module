package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.StorageBucketViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.query.ListStorageBucketsQuery;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StorageBucket;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListStorageBucketsUseCase 테스트")
class ListStorageBucketsUseCaseTest {

    @Mock
    private StorageBucketRepository storageBucketRepository;

    private ListStorageBucketsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListStorageBucketsUseCase(storageBucketRepository, new StorageBucketViewAssembler());
    }

    @Test
    @DisplayName("성공 - 버킷 목록 페이징")
    void apply_success() {
        // given
        var bucket = new StorageBucket(1L, "erp-bucket", "us-east-1", "ERP Bucket", LocalDateTime.now());
        given(storageBucketRepository.search(java.util.Optional.empty(), 0, 10))
                .willReturn(PageResult.of(List.of(bucket), 0, 10, 1));

        // when
        var result = useCase.apply(new ListStorageBucketsQuery(null, 0, 10));

        // then
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().getFirst().bucketCode()).isEqualTo("erp-bucket");
    }
}
