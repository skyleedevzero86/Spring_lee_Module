package com.sleekydz86.ocrstudy1.application.port.out;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;

import java.io.InputStream;

public interface DocumentTypeDetectionPort {
    DocumentType detectDocumentType(InputStream imageStream, String ocrText, String filename);
}
