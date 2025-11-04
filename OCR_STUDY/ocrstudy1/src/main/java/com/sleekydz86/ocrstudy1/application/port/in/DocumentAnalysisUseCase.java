package com.sleekydz86.ocrstudy1.application.port.in;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;

public interface DocumentAnalysisUseCase {
    DocumentAnalysis analyzeDocument(Long imageId);
    DocumentAnalysis verifyDocument(Long imageId);
    DocumentAnalysis summarizeDocument(Long imageId);
}