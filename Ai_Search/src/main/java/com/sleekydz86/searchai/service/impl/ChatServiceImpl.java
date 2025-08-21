package com.sleekydz86.searchai.service.impl;

import com.sleekydz86.searchai.global.beans.ChatEntity;
import com.sleekydz86.searchai.global.beans.Document;
import com.sleekydz86.searchai.global.beans.SearchResult;
import com.sleekydz86.searchai.global.enums.ChatMode;
import com.sleekydz86.searchai.global.enums.SSEMsgType;
import com.sleekydz86.searchai.service.ChatService;
import com.sleekydz86.searchai.service.DocumentService;
import com.sleekydz86.searchai.service.SearXngService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    @Resource
    private DocumentService documentService;

    @Resource
    private SearXngService searXngService;

    private static final String RAG_PROMPT_TEMPLATE = """
            아래 제공된 컨텍스트 지식 베이스 내용을 기반으로 사용자의 질문에 답변해 주세요.
            규칙:
            1. 답변 시 컨텍스트 정보를 충분히 활용하되, "컨텍스트에 따르면", "지식 베이스에 따르면"과 같은 문구를 직접 언급하지 마세요.
            2. 컨텍스트에 질문에 답할 충분한 정보가 없다면, "기존 지식으로는 이 질문에 답변할 수 없습니다."라고 명확하게 알려주세요.
            3. 답변은 직접적이고, 명확하며, 관련성이 있어야 합니다.
            【컨텍스트】
            {context}
                                    
            【질문】
            {question}
            """;

    // 추가: 인터넷 검색 모드용 프롬프트 템플릿
    private static final String INTERNET_SEARCH_PROMPT_TEMPLATE = """
            당신은 실시간 인터넷 검색 능력을 갖춘 스마트 비서입니다. 아래 제공된 최신 인터넷 검색 결과를 바탕으로 사용자의 질문에 답변해 주세요.
            규칙:
            1. 모든 검색 결과를 종합적으로 분석하여 사용자에게 포괄적이고 정확하며 일관된 답변을 제공하세요.
            2. 답변에서 "검색 결과에 따르면..."과 같은 문구를 직접 인용하지 말고, 자연스럽게 문장을 구성하세요.
            3. 검색 결과에 충분한 정보가 없다면, "현재 검색 결과로는 질문에 대한 정확한 정보를 찾을 수 없습니다."라고 솔직하게 알려주세요.
            4. 답변은 간결하고 요점을 명확히 해야 합니다.
            【인터넷 검색 결과】
            {context}
                                    
            【사용자 질문】
            {question}
            """;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void streamChat(ChatEntity chatEntity) {
        String userId = chatEntity.getCurrentUserName();
        String question = chatEntity.getMessage();
        // 프런트엔드에서 전달된 모드를 가져오고, 없으면 기본적으로 직접 대화 모드로 설정
        ChatMode mode = chatEntity.getMode() != null ? chatEntity.getMode() : ChatMode.DIRECT;
        Prompt prompt;

        // 모드에 따라 다른 로직을 선택
        switch (mode) {
            case KNOWLEDGE_BASE:
                log.info("【사용자: {}】가 【지식 베이스 모드】로 질문합니다.", userId);
                prompt = createRagPrompt(question);
                break;
            case INTERNET_SEARCH:
                log.info("【사용자: {}】가 【인터넷 검색 모드】로 질문합니다.", userId);
                prompt = createInternetSearchPrompt(question);
                break;
            case DIRECT:
            default:
                log.info("【사용자: {}】가 【직접 대화 모드】로 질문합니다.", userId);
                prompt = new Prompt(question);
                break;
        }

        Flux<String> stream = chatClient.prompt(prompt).stream().content();

        stream.doOnError(throwable -> {
                    log.error("【사용자: {}】의 AI 스트림 처리 중 오류 발생: {}", userId, throwable.getMessage(), throwable);
                    SSEServer.sendMsg(userId, "죄송합니다, 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.", SSEMsgType.FINISH);
                    SSEServer.close(userId);
                })
                .subscribe(
                        content -> SSEServer.sendMsg(userId, content, SSEMsgType.ADD),
                        error -> log.error("【사용자: {}】의 스트림 구독 최종 실패: {}", userId, error.getMessage()),
                        () -> {
                            log.info("【사용자: {}】의 스트림이 성공적으로 종료되었습니다.", userId);
                            SSEServer.sendMsg(userId, "done", SSEMsgType.FINISH);
                            SSEServer.close(userId);
                        }
                );
    }

    /**
     * RAG (지식 베이스) 모드용 프롬프트 생성
     */
    private Prompt createRagPrompt(String question) {
        List<Document> relatedDocs = documentService.doSearch(question);
        String context = "관련된 지식 베이스 정보를 찾지 못했습니다.";
        if (!CollectionUtils.isEmpty(relatedDocs)) {
            context = relatedDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n---\n"));
        }
        String promptContent = RAG_PROMPT_TEMPLATE
                .replace("{context}", context)
                .replace("{question}", question);
        return new Prompt(promptContent);
    }

    /**
     * 인터넷 검색 모드용 프롬프트 생성
     */
    private Prompt createInternetSearchPrompt(String question) {
        // 1. 인터넷 검색 실행
        List<SearchResult> searchResults = searXngService.search(question);
        String context = "유효한 인터넷 검색 결과를 가져오지 못했습니다.";

        // 2. 컨텍스트 구성
        if (!CollectionUtils.isEmpty(searchResults)) {
            // 검색 결과를 명확한 컨텍스트 텍스트로 형식화
            context = searchResults.stream()
                    .map(result -> String.format("【출처 제목】: %s\n【내용 요약】: %s\n【링크】: %s",
                            result.getTitle(),
                            result.getContent(),
                            result.getUrl()))
                    .collect(Collectors.joining("\n\n---\n\n"));
        }

        // 3. 프롬프트 생성
        String promptContent = INTERNET_SEARCH_PROMPT_TEMPLATE
                .replace("{context}", context)
                .replace("{question}", question);
        return new Prompt(promptContent);
    }
}