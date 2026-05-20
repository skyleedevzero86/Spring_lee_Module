package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestFixtures;

@Tag("integration")
@DisplayName("파일 업로드 통합 테스트")
class FileUploadIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectStoragePort objectStorage;

    @Test
    @DisplayName("성공 - 이미지 업로드 후 DB·S3 저장")
    void uploadImage_dbAndS3_success() {
        // given
        var request = IntegrationTestFixtures.imageUploadEntity();

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/files/upload",
                request,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("it_");

        var files = fileRepository.search(java.util.Optional.empty(), 0, 1);
        assertThat(files.totalElements()).isGreaterThanOrEqualTo(1);
        var saved = files.content().getFirst();
        assertThat(objectStorage.exists(saved.objectKey())).isTrue();
        assertThat(saved.thumbnailKey()).isNotBlank();
        assertThat(objectStorage.exists(saved.thumbnailKey())).isTrue();
    }
}
