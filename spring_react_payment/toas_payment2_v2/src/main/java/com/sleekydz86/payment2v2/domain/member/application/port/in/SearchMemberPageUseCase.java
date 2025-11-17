package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.SearchMemberResponse;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface SearchMemberPageUseCase {
    PageResponse<SearchMemberResponse> searchByName(String name, Pageable pageable);
    PageResponse<SearchMemberResponse> searchByEmail(String email, Pageable pageable);
    PageResponse<SearchMemberResponse> searchAll(Pageable pageable);
}

