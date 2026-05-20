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
@DisplayName("PDF 업로드 통합 테스트")
class PdfUploadIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ObjectStoragePort objectStorage;

    @Test
    @DisplayName("성공 - PDF 업로드 후 DB·S3·media_type PDF")
    void uploadPdf_dbAndS3_success() {
        // given
        var request = IntegrationTestFixtures.pdfUploadEntity();

        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/files/upload",
                request,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("application/pdf");

        var page = fileRepository.search(java.util.Optional.of("sample.pdf"), 0, 5);
        assertThat(page.content()).isNotEmpty();
        var saved = page.content().getFirst();
        assertThat(saved.contentType()).isEqualTo("application/pdf");
        assertThat(saved.mediaType()).isEqualTo("PDF");
        assertThat(objectStorage.exists(saved.objectKey())).isTrue();
        if (saved.thumbnailKey() != null && !saved.thumbnailKey().isBlank()) {
            assertThat(objectStorage.exists(saved.thumbnailKey())).isTrue();
        }
    }
}
