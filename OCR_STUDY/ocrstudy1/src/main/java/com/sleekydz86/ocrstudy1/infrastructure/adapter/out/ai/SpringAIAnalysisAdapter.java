package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.ai;

import com.sleekydz86.ocrstudy1.application.port.out.AIAnalysisPort;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAIAnalysisAdapter implements AIAnalysisPort {

    private final ChatClient chatClient;

    @Override
    public DocumentAnalysis analyzeDocument(String ocrText, String documentType, String filename) {
        try {
            String promptText = buildAnalysisPrompt(ocrText, documentType, filename);

            ChatResponse response = chatClient.prompt().user(promptText).call().chatResponse();
            String aiResponse = response.getResult().getOutput().getContent();

            return parseAIResponse(aiResponse, ocrText, documentType);

        } catch (Exception e) {
            log.error("AI analysis failed", e);
            return createFallbackAnalysis(documentType);
        }
    }

    @Override
    public DocumentAnalysis verifyDocument(String ocrText, String documentType, Map<String, String> extractedInfo) {
        try {
            String promptText = buildVerificationPrompt(ocrText, documentType, extractedInfo);
            ChatResponse response = chatClient.prompt().user(promptText).call().chatResponse();
            String aiResponse = response.getResult().getOutput().getContent();

            return parseVerificationResponse(aiResponse, ocrText, documentType, extractedInfo);

        } catch (Exception e) {
            log.error("AI verification failed", e);
            return createFallbackVerification(extractedInfo);
        }
    }

    @Override
    public String summarizeDocument(String ocrText, String documentType) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate("""
                다음은 {documentType} 문서의 OCR로 추출한 텍스트입니다.
                이 문서의 주요 내용을 3-5문장으로 요약해주세요.
                중요한 정보(날짜, 금액, 이름 등)는 포함해야 합니다.
                
                문서 종류: {documentType}
                OCR 텍스트:
                {ocrText}
                
                요약:
                """);

            Map<String, Object> variables = Map.of(
                    "documentType", documentType != null ? documentType : "문서",
                    "ocrText", ocrText != null ? ocrText : ""
            );

            String promptText = promptTemplate.render(variables);
            ChatResponse response = chatClient.prompt().user(promptText).call().chatResponse();
            String summary = response.getResult().getOutput().getContent();

            return summary != null ? summary.trim() : "요약을 생성할 수 없습니다.";

        } catch (Exception e) {
            log.error("Document summarization failed", e);
            return "AI 요약 생성 실패: " + e.getMessage();
        }
    }

    private String buildAnalysisPrompt(String ocrText, String documentType, String filename) {
        return String.format("""
            당신은 문서 분석 전문가입니다. 다음 OCR 텍스트를 분석해주세요.
            
            문서 종류: %s
            파일명: %s
            
            OCR 텍스트:
            %s
            
            다음 형식으로 분석 결과를 제공해주세요:
            1. 문서 요약 (3-5문장)
            2. 진위 여부 점수 (0-1, 1이 가장 진짜)
            3. 감지된 문제점 (있다면)
            4. 추출된 주요 필드 (키-값 쌍)
            5. 분석 인사이트
            
            형식:
            SUMMARY: [요약 텍스트]
            AUTHENTICITY: [0-1 사이 점수]
            ISSUES: [문제점1, 문제점2] (없으면 NONE)
            FIELDS: [필드명1:값1, 필드명2:값2]
            INSIGHTS: [인사이트 텍스트]
            """, documentType, filename, ocrText);
    }

    private String buildVerificationPrompt(String ocrText, String documentType, Map<String, String> extractedInfo) {
        StringBuilder infoText = new StringBuilder();
        if (extractedInfo != null) {
            extractedInfo.forEach((key, value) -> infoText.append(key).append(": ").append(value).append("\n"));
        }

        return String.format("""
            다음 신분증/문서의 진위를 검증해주세요.
            
            문서 종류: %s
            OCR 텍스트:
            %s
            
            추출된 정보:
            %s
            
            다음을 검증해주세요:
            1. 정보 일관성 (OCR 텍스트와 추출된 정보가 일치하는지)
            2. 문서 형식의 정확성
            3. 의심스러운 부분 발견 여부
            
            형식:
            AUTHENTIC: [true/false]
            SCORE: [0-1 사이 점수]
            ISSUES: [문제점들] (없으면 NONE)
            """, documentType, ocrText, infoText.toString());
    }

    private DocumentAnalysis parseAIResponse(String aiResponse, String ocrText, String documentType) {
        DocumentAnalysis.DocumentAnalysisBuilder builder = DocumentAnalysis.builder();
        builder.imageId(null);

        List<String> issues = new ArrayList<>();
        Map<String, Object> fields = new HashMap<>();

        try {
            String[] lines = aiResponse.split("\n");
            String summary = "";
            Double authenticity = 0.7;
            String insights = "";

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("SUMMARY:")) {
                    summary = line.substring(8).trim();
                } else if (line.startsWith("AUTHENTICITY:")) {
                    try {
                        authenticity = Double.parseDouble(line.substring(14).trim());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse authenticity score");
                    }
                } else if (line.startsWith("ISSUES:")) {
                    String issuesStr = line.substring(7).trim();
                    if (!issuesStr.equals("NONE") && !issuesStr.isEmpty()) {
                        issues.addAll(Arrays.asList(issuesStr.split(",")));
                    }
                } else if (line.startsWith("FIELDS:")) {
                    String fieldsStr = line.substring(7).trim();
                    if (!fieldsStr.isEmpty()) {
                        for (String field : fieldsStr.split(",")) {
                            String[] kv = field.split(":");
                            if (kv.length == 2) {
                                fields.put(kv[0].trim(), kv[1].trim());
                            }
                        }
                    }
                } else if (line.startsWith("INSIGHTS:")) {
                    insights = line.substring(9).trim();
                }
            }

            builder.summary(summary.isEmpty() ? "AI 분석 완료" : summary)
                    .authenticityScore(authenticity)
                    .isAuthentic(authenticity >= 0.7)
                    .detectedIssues(issues)
                    .extractedFields(fields)
                    .aiInsights(insights.isEmpty() ? "정상적인 문서로 판단됩니다." : insights)
                    .confidence(0.85);

        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            return createFallbackAnalysis(documentType);
        }

        return builder.build();
    }

    private DocumentAnalysis parseVerificationResponse(String aiResponse, String ocrText,
                                                       String documentType, Map<String, String> extractedInfo) {
        DocumentAnalysis.DocumentAnalysisBuilder builder = DocumentAnalysis.builder();
        builder.imageId(null);
        builder.summary("문서 검증 완료");

        List<String> issues = new ArrayList<>();
        boolean isAuthentic = true;
        double score = 0.8;

        try {
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("AUTHENTIC:")) {
                    isAuthentic = Boolean.parseBoolean(line.substring(10).trim());
                } else if (line.startsWith("SCORE:")) {
                    try {
                        score = Double.parseDouble(line.substring(6).trim());
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse verification score");
                    }
                } else if (line.startsWith("ISSUES:")) {
                    String issuesStr = line.substring(7).trim();
                    if (!issuesStr.equals("NONE") && !issuesStr.isEmpty()) {
                        issues.addAll(Arrays.asList(issuesStr.split(",")));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse verification response", e);
        }

        builder.authenticityScore(score)
                .isAuthentic(isAuthentic && score >= 0.7)
                .detectedIssues(issues)
                .extractedFields(Map.of("verification", "completed"))
                .aiInsights(isAuthentic ? "문서가 진위 검증을 통과했습니다." : "문서의 진위를 의심할 수 있습니다.")
                .confidence(0.9);

        return builder.build();
    }

    private DocumentAnalysis createFallbackAnalysis(String documentType) {
        return DocumentAnalysis.builder()
                .summary("AI 분석을 수행할 수 없습니다. 기본 분석 결과를 제공합니다.")
                .authenticityScore(0.5)
                .isAuthentic(false)
                .detectedIssues(List.of("AI 분석 서비스를 사용할 수 없음"))
                .extractedFields(new HashMap<>())
                .aiInsights("기본 분석 모드입니다.")
                .confidence(0.3)
                .build();
    }

    private DocumentAnalysis createFallbackVerification(Map<String, String> extractedInfo) {
        return DocumentAnalysis.builder()
                .summary("AI 검증을 수행할 수 없습니다.")
                .authenticityScore(0.5)
                .isAuthentic(false)
                .detectedIssues(List.of("AI 검증 서비스를 사용할 수 없음"))
                .extractedFields(extractedInfo != null ? Map.copyOf(extractedInfo) : new HashMap<>())
                .aiInsights("기본 검증 모드입니다.")
                .confidence(0.3)
                .build();
    }
}