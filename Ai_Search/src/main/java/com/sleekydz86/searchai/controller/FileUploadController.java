package com.sleekydz86.searchai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rag")
@Slf4j
public class FileUploadController {

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("status", 400);
                response.put("msg", "업로드할 파일이 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("파일 업로드 요청: 파일명={}, 크기={} bytes", file.getOriginalFilename(), file.getSize());

            response.put("status", 200);
            response.put("msg", "파일이 성공적으로 업로드되었습니다.");
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());

            log.info("파일 업로드 성공: {}", file.getOriginalFilename());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);

            response.put("status", 500);
            response.put("msg", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getUploadStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("msg", "Upload service is running");
        response.put("maxFileSize", "10MB");
        response.put("supportedFormats", new String[]{"txt", "pdf", "md", "docx"});

        return ResponseEntity.ok(response);
    }
}