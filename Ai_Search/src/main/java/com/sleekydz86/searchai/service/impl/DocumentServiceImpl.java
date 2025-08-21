package com.sleekydz86.searchai.service.impl;

import com.sleekydz86.searchai.service.DocumentService;
import com.sleekydz86.searchai.global.beans.Document;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    @Override
    public List<Document> doSearch(String question) {
        log.info("지식 베이스에서 질문에 대한 관련 문서 검색: {}", question);

        List<Document> documents = new ArrayList<>();

        if (question != null && !question.trim().isEmpty()) {
            Document sampleDoc = new Document(
                    "샘플 문서",
                    "이것은 '" + question + "'에 대한 샘플 응답입니다. 실제 벡터 검색 구현이 필요합니다."
            );
            documents.add(sampleDoc);
        }

        log.debug("검색된 문서 수: {}", documents.size());
        return documents;
    }
}