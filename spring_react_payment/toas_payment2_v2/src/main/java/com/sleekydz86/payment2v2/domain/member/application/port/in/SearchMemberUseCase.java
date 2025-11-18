package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.SearchMemberResponse;

import java.util.List;

public interface SearchMemberUseCase {
    List<SearchMemberResponse> searchByName(String name);
    List<SearchMemberResponse> searchByEmail(String email);
    List<SearchMemberResponse> searchAll();
}
