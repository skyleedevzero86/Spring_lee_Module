package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestFixtures;

@Tag("integration")
@DisplayName("파일 삭제 통합 테스트")
class DeleteFileIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectStoragePort objectStorage;

    @Test
    @DisplayName("성공 - 삭제 시 DB·S3 원본·썸네일 제거")
    void deleteFile_removesDbAndS3_success() {
        // given
        restTemplate.postForEntity(
                baseUrl() + "/api/files/upload",
                IntegrationTestFixtures.imageUploadEntity(),
                String.class);

        var saved = fileRepository.search(java.util.Optional.of("sample.png"), 0, 1).content().getFirst();
        String objectKey = saved.objectKey();
        String thumbnailKey = saved.thumbnailKey();
        assertThat(objectStorage.exists(objectKey)).isTrue();

        // when
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/api/files/" + saved.id(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(fileRepository.findById(saved.id())).isEmpty();
        assertThat(objectStorage.exists(objectKey)).isFalse();
        if (thumbnailKey != null && !thumbnailKey.isBlank()) {
            assertThat(objectStorage.exists(thumbnailKey)).isFalse();
        }
    }
}
