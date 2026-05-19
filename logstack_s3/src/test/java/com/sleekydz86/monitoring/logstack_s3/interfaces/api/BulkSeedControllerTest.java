package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sleekydz86.monitoring.logstack_s3.application.usecase.SeedFilesUseCase;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;

@ExtendWith(MockitoExtension.class)
@DisplayName("BulkSeedController API 테스트")
class BulkSeedControllerTest {

    private MockMvc mockMvc;
    private SeedFilesUseCase seedFilesUseCase;

    @BeforeEach
    void setUp() {
        // given
        seedFilesUseCase = mock(SeedFilesUseCase.class);
        BulkSeedController controller = new BulkSeedController(seedFilesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("성공 - 시드 API")
    void seed_success() throws Exception {
        // given
        given(seedFilesUseCase.apply(10)).willReturn(10);

        // when & then
        mockMvc.perform(post("/api/admin/seed").param("count", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seeded").value(10))
                .andExpect(jsonPath("$.message").value(KoreanMessages.seedComplete(10)));
    }

    @Test
    @DisplayName("실패 - 시드 건수 오류")
    void seed_invalidCount_fail() throws Exception {
        // given
        given(seedFilesUseCase.apply(0)).willThrow(new InvalidRequestException(KoreanMessages.SEED_COUNT_MIN));

        // when & then
        mockMvc.perform(post("/api/admin/seed").param("count", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(KoreanMessages.SEED_COUNT_MIN));
    }
}
