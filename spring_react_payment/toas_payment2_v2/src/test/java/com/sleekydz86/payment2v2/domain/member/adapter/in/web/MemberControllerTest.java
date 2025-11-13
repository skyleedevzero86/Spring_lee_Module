package com.sleekydz86.payment2v2.domain.member.adapter.in.web;

import com.sleekydz86.payment2v2.domain.member.adapter.in.web.dto.RegisterMemberRequest;
import com.sleekydz86.payment2v2.domain.member.application.dto.RegisterMemberResponse;
import com.sleekydz86.payment2v2.domain.member.application.port.in.RegisterMemberUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@DisplayName("MemberController 테스트")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterMemberUseCase registerMemberUseCase;

    @MockBean
    private MemberWebMapper memberWebMapper;

    @Test
    @DisplayName("회원가입 API가 성공적으로 동작한다")
    void 회원가입_API가_성공적으로_동작한다() throws Exception {
        // given
        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("test@example.com");
        request.setName("홍길동");
        request.setPassword("password123");

        RegisterMemberResponse response = RegisterMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .build();

        given(memberWebMapper.toCommand(any(RegisterMemberRequest.class))).willReturn(null);
        given(registerMemberUseCase.register(any())).willReturn(response);
        given(memberWebMapper.toApiResponse(response)).willReturn(null);

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("회원가입 API에서 필수 필드 누락 시 400 에러가 발생한다")
    void 회원가입_API에서_필수_필드_누락_시_400_에러가_발생한다() throws Exception {
        // given
        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("test@example.com");

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 API에서 잘못된 이메일 형식 시 400 에러가 발생한다")
    void 회원가입_API에서_잘못된_이메일_형식_시_400_에러가_발생한다() throws Exception {
        // given
        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("invalid-email");
        request.setName("홍길동");
        request.setPassword("password123");

        // when & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

