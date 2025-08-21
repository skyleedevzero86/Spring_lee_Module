package com.sleekydz86.searchai.global.enums;

public enum ChatMode {
    /**
     * 직접 대화 모드
     */
    DIRECT,

    /**
     * 업로드된 문서를 기반으로 하는 지식 베이스 모드 (RAG)
     */
    KNOWLEDGE_BASE,

    /**
     * 인터넷 검색 모드
     */
    INTERNET_SEARCH
}