package com.sleekydz86.payment2v2.domain.member.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MemberName 값 객체 테스트")
class MemberNameTest {

    @Test
    @DisplayName("유효한 이름으로 MemberName 객체를 생성할 수 있다")
    void 유효한_이름으로_MemberName_객체를_생성할_수_있다() {
        // given
        String nameValue = "홍길동";

        // when
        MemberName memberName = MemberName.of(nameValue);

        // then
        assertThat(memberName.getValue()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이름 앞뒤 공백이 제거되어 저장된다")
    void 이름_앞뒤_공백이_제거되어_저장된다() {
        // given
        String nameValue = "  홍길동  ";

        // when
        MemberName memberName = MemberName.of(nameValue);

        // then
        assertThat(memberName.getValue()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("null 값으로 MemberName 객체를 생성하면 예외가 발생한다")
    void null_값으로_MemberName_객체를_생성하면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> MemberName.of(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열로 MemberName 객체를 생성하면 예외가 발생한다")
    void 빈_문자열로_MemberName_객체를_생성하면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> MemberName.of(""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> MemberName.of("   "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최대 길이를 초과하는 이름으로 MemberName 객체를 생성하면 예외가 발생한다")
    void 최대_길이를_초과하는_이름으로_MemberName_객체를_생성하면_예외가_발생한다() {
        // given
        String longName = "가".repeat(ValidationConstants.MAX_NAME_LENGTH + 1);

        // when & then
        assertThatThrownBy(() -> MemberName.of(longName))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최대 길이 이름으로 MemberName 객체를 생성할 수 있다")
    void 최대_길이_이름으로_MemberName_객체를_생성할_수_있다() {
        // given
        String maxLengthName = "가".repeat(ValidationConstants.MAX_NAME_LENGTH);

        // when
        MemberName memberName = MemberName.of(maxLengthName);

        // then
        assertThat(memberName.getValue()).isEqualTo(maxLengthName);
    }

    @Test
    @DisplayName("영문 이름으로 MemberName 객체를 생성할 수 있다")
    void 영문_이름으로_MemberName_객체를_생성할_수_있다() {
        // given
        String nameValue = "John Doe";

        // when
        MemberName memberName = MemberName.of(nameValue);

        // then
        assertThat(memberName.getValue()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("한글과 영문이 혼합된 이름으로 MemberName 객체를 생성할 수 있다")
    void 한글과_영문이_혼합된_이름으로_MemberName_객체를_생성할_수_있다() {
        // given
        String nameValue = "홍길동 Hong";

        // when
        MemberName memberName = MemberName.of(nameValue);

        // then
        assertThat(memberName.getValue()).isEqualTo("홍길동 Hong");
    }
}

