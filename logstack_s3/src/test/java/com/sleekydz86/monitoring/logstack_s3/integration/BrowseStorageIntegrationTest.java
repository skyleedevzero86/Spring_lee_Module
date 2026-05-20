package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.ListStorageBucketsQuery;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.BrowseStorageUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.ListStorageBucketsUseCase;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestFixtures;

@Tag("integration")
@DisplayName("S3 스토리지 조회 통합 테스트")
class BrowseStorageIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ListStorageBucketsUseCase listStorageBucketsUseCase;

    @Autowired
    private BrowseStorageUseCase browseStorageUseCase;

    @Test
    @DisplayName("성공 - 버킷 목록·객체 목록·화면")
    void storageFlow_success() {
        // given
        var request = IntegrationTestFixtures.imageUploadEntity();
        ResponseEntity<String> upload = restTemplate.postForEntity(
                baseUrl() + "/api/files/upload",
                request,
                String.class);
        assertThat(upload.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when
        var buckets = listStorageBucketsUseCase.apply(new ListStorageBucketsQuery(null, 0, 10));
        String bucketCode = buckets.content().getFirst().bucketCode();
        var objects = browseStorageUseCase.apply(
                new BrowseStorageQuery(bucketCode, null, StorageObjectPaths.PREFIX_ALL, 0, 12));
        ResponseEntity<String> bucketPage = restTemplate.getForEntity(baseUrl() + "/storage", String.class);
        ResponseEntity<String> objectPage = restTemplate.getForEntity(
                baseUrl() + "/storage/buckets/" + bucketCode,
                String.class);

        // then
        assertThat(buckets.totalElements()).isGreaterThanOrEqualTo(1);
        assertThat(objects.page().totalElements()).isGreaterThanOrEqualTo(2);
        assertThat(bucketPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(bucketPage.getBody()).contains("스토리지 버킷");
        assertThat(objectPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectPage.getBody()).contains(bucketCode);
    }
}
