package com.sleekydz86.monitoring.logstack_s3.support;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.sleekydz86.monitoring.logstack_s3.global.config.AdminApiKeyFilter;

public final class IntegrationTestFixtures {

    public static final String ADMIN_API_KEY = "test-integration-key";

    private IntegrationTestFixtures() {
    }

    public static HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AdminApiKeyFilter.ADMIN_API_KEY_HEADER, ADMIN_API_KEY);
        return headers;
    }

    public static HttpEntity<MultiValueMap<String, Object>> imageUploadEntity() {
        return multipartEntity("sample.png", "image/png", pngBytes());
    }

    public static HttpEntity<MultiValueMap<String, Object>> pdfUploadEntity() {
        return multipartEntity("sample.pdf", "application/pdf", minimalPdfBytes());
    }

    public static byte[] pngBytes() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00};
    }

    public static byte[] minimalPdfBytes() {
        String pdf = """
                %PDF-1.4
                1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj
                2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj
                3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] >>endobj
                xref
                0 4
                trailer<< /Root 1 0 R /Size 4 >>
                startxref
                200
                %%EOF
                """;
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    private static HttpEntity<MultiValueMap<String, Object>> multipartEntity(
            String filename,
            String contentType,
            byte[] bytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        return new HttpEntity<>(body, headers);
    }
}
