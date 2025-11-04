package com.sleekydz86.ocrstudy1.application.port.out;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;

import java.util.Map;

public interface AIAnalysisPort {
    DocumentAnalysis analyzeDocument(String ocrText, String documentType, String filename);
    DocumentAnalysis verifyDocument(String ocrText, String documentType, Map<String, String> extractedInfo);
    String summarizeDocument(String ocrText, String documentType);
}