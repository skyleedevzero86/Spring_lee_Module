package com.sleekydz86.payment2v2.domain.member.adapter.in.web;

import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.*;
import com.sleekydz86.payment2v2.domain.member.application.dto.FindMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.RegisterMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.ResetPasswordResponse;
import com.sleekydz86.payment2v2.domain.member.application.dto.SearchMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.port.in.FindMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.RegisterMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.ResetPasswordUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.SearchMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.SearchMemberPageUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PageResponse;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final FindMemberUseCase findMemberUseCase;
    private final SearchMemberUseCase searchMemberUseCase;
    private final SearchMemberPageUseCase searchMemberPageUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final MemberWebMapper memberWebMapper;

    @PostMapping
    public ResponseEntity<RegisterMemberApiResponse> register(@Valid @RequestBody RegisterMemberRequest request) {
        return LoggingUtil.executeWithContext("email", request.getEmail() != null ? request.getEmail() : "알수없음", () -> {
            RegisterMemberResponse response = registerMemberUseCase.register(memberWebMapper.toCommand(request));
            RegisterMemberApiResponse apiResponse = memberWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        });
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<FindMemberApiResponse> findByEmail(@PathVariable String email) {
        Email.of(email);
        return LoggingUtil.executeWithContext("email", email, () -> {
            FindMemberResponse response = findMemberUseCase.findByEmail(email);
            FindMemberApiResponse apiResponse = memberWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindMemberApiResponse> findById(@PathVariable Long id) {
        MemberId memberId = MemberId.of(id);
        return LoggingUtil.executeWithContext("memberId", String.valueOf(memberId.getValue()), () -> {
            FindMemberResponse response = findMemberUseCase.findById(id);
            FindMemberApiResponse apiResponse = memberWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<SearchMemberApiResponse>> searchByName(@RequestParam String name) {
        MemberName.of(name);
        List<SearchMemberResponse> responses = searchMemberUseCase.searchByName(name);
        List<SearchMemberApiResponse> apiResponses = responses.stream()
                .map(memberWebMapper::toApiResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponses);
    }

    @GetMapping("/search/email")
    public ResponseEntity<List<SearchMemberApiResponse>> searchByEmail(@RequestParam String email) {
        Email.of(email);
        List<SearchMemberResponse> responses = searchMemberUseCase.searchByEmail(email);
        List<SearchMemberApiResponse> apiResponses = responses.stream()
                .map(memberWebMapper::toApiResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponses);
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<SearchMemberApiResponse>> searchAll() {
        List<SearchMemberResponse> responses = searchMemberUseCase.searchAll();
        List<SearchMemberApiResponse> apiResponses = responses.stream()
                .map(memberWebMapper::toApiResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponses);
    }

    @GetMapping("/search/name/page")
    public ResponseEntity<com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse>> searchByNamePage(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable) {
        MemberName.of(name);
        PageResponse<SearchMemberResponse> pageResponse = searchMemberPageUseCase.searchByName(name, pageable);
        com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse> apiResponse = 
                memberWebMapper.toPageApiResponse(pageResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/search/email/page")
    public ResponseEntity<com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse>> searchByEmailPage(
            @RequestParam String email,
            @PageableDefault(size = 20) Pageable pageable) {
        Email.of(email);
        PageResponse<SearchMemberResponse> pageResponse = searchMemberPageUseCase.searchByEmail(email, pageable);
        com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse> apiResponse = 
                memberWebMapper.toPageApiResponse(pageResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/search/all/page")
    public ResponseEntity<com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse>> searchAllPage(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<SearchMemberResponse> pageResponse = searchMemberPageUseCase.searchAll(pageable);
        com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse<SearchMemberApiResponse> apiResponse = 
                memberWebMapper.toPageApiResponse(pageResponse);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return LoggingUtil.executeWithContext("email", request.getEmail() != null ? request.getEmail() : "알수없음", () -> {
            ResetPasswordResponse response = resetPasswordUseCase.resetPassword(memberWebMapper.toCommand(request));
            ResetPasswordApiResponse apiResponse = memberWebMapper.toApiResponse(response);
            return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
        });
    }
}

