package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.BrowseStorageUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestFixtures;

@Tag("integration")
@DisplayName("S3 스토리지 조회 통합 테스트")
class BrowseStorageIntegrationTest extends IntegrationTestBase {

    @Autowired
    private BrowseStorageUseCase browseStorageUseCase;

    @Autowired
    private ObjectStoragePort objectStorage;

    @Test
    @DisplayName("성공 - 업로드 후 스토리지 화면·목록 API")
    void browseStorage_afterUpload_success() {
        // given
        var request = IntegrationTestFixtures.imageUploadEntity();
        ResponseEntity<String> upload = restTemplate.postForEntity(
                baseUrl() + "/api/files/upload",
                request,
                String.class);
        assertThat(upload.getStatusCode()).isEqualTo(HttpStatus.OK);

        // when
        var browse = browseStorageUseCase.apply(new BrowseStorageQuery(null, StorageObjectPaths.PREFIX_ALL));
        ResponseEntity<String> page = restTemplate.getForEntity(baseUrl() + "/storage", String.class);

        // then
        assertThat(browse.objectCount()).isGreaterThanOrEqualTo(2);
        assertThat(browse.objects()).anyMatch(o -> o.key().startsWith("uploads/"));
        assertThat(browse.objects()).anyMatch(o -> o.key().startsWith("thumbnails/"));
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page.getBody()).contains("스토리지");
        assertThat(page.getBody()).contains(objectStorage.bucketName());
    }
}
