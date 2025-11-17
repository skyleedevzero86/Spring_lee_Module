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
@DisplayName("MemberController ?�스??)
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
    @DisplayName("?�원가??API가 ?�공?�으�??�작?�다")
    void ?�원가??API가_?�공?�으�??�작?�다() throws Exception {

        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("test@example.com");
        request.setName("?�길??);
        request.setPassword("password123");

        RegisterMemberResponse response = RegisterMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("?�길??)
                .build();

        given(memberWebMapper.toCommand(any(RegisterMemberRequest.class))).willReturn(null);
        given(registerMemberUseCase.register(any())).willReturn(response);
        given(memberWebMapper.toApiResponse(response)).willReturn(null);

 & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("?�원가??API?�서 ?�수 ?�드 ?�락 ??400 ?�러가 발생?�다")
    void ?�원가??API?�서_?�수_?�드_?�락_??400_?�러가_발생?�다() throws Exception {

        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("test@example.com");

 & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("?�원가??API?�서 ?�못???�메???�식 ??400 ?�러가 발생?�다")
    void ?�원가??API?�서_?�못???�메???�식_??400_?�러가_발생?�다() throws Exception {

        RegisterMemberRequest request = new RegisterMemberRequest();
        request.setEmail("invalid-email");
        request.setName("?�길??);
        request.setPassword("password123");

 & then
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

