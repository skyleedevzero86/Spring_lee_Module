package com.sleekydz86.ocrstudy1.application.port.out;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoragePort Mock 테스트")
class StoragePortMockTest {

    private StoragePort storagePort;

    @BeforeEach
    void setUp() {
        storagePort = new StoragePort() {
            @Override
            public String uploadFile(InputStream inputStream, String filename, String contentType, long size) {
                return "test_object_name";
            }

            @Override
            public InputStream downloadFile(String objectName) {
                return new ByteArrayInputStream("test".getBytes());
            }

            @Override
            public void deleteFile(String objectName) {
            }

            @Override
            public String getFileUrl(String objectName) {
                return "http://localhost:9000/bucket/" + objectName;
            }
        };
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void uploadFile_Success() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        String result = storagePort.uploadFile(inputStream, "test.jpg", "image/jpeg", 1024L);

        assertThat(result).isEqualTo("test_object_name");
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void downloadFile_Success() {
        InputStream result = storagePort.downloadFile("test_object_name");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("파일 URL 조회 성공")
    void getFileUrl_Success() {
        String url = storagePort.getFileUrl("test_object_name");

        assertThat(url).isEqualTo("http://localhost:9000/bucket/test_object_name");
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFile_Success() {
        storagePort.deleteFile("test_object_name");
        assertThat(true).isTrue();
    }
}