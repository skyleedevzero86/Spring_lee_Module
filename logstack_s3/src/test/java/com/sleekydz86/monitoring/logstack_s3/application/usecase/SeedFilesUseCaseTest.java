package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeedFilesUseCase 테스트")
class SeedFilesUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private SeedFilesUseCase useCase;

    @Test
    @DisplayName("성공 - 시드 실행")
    void apply_success() {
        // given
        int count = 100;

        // when
        Integer result = useCase.apply(count);

        // then
        assertThat(result).isEqualTo(100);
        verify(fileRepository).seedDemoData(100);
    }

    @Test
    @DisplayName("실패 - 건수 0")
    void apply_zero_fail() {
        // given
        int count = 0;

        // when & then
        assertThatThrownBy(() -> useCase.apply(count))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage(KoreanMessages.SEED_COUNT_MIN);
    }

    @Test
    @DisplayName("실패 - 건수 초과")
    void apply_overLimit_fail() {
        // given
        int count = 500_001;

        // when & then
        assertThatThrownBy(() -> useCase.apply(count))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage(KoreanMessages.SEED_COUNT_MAX);
    }
}
