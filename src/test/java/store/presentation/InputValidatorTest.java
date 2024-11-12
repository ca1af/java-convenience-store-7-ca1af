package store.presentation;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InputValidator 테스트")
class InputValidatorTest {

    @Test
    @DisplayName("유효한 상품 입력을 검증한다")
    void validateProduct_ValidInput_ShouldNotThrow() {
        String validInput = "[콜라-10],[사이다-5],[후추-5]";

        Assertions.assertThatCode(() -> InputValidator.validateProduct(validInput)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("상품 입력이 비어있을 경우 예외를 발생시킨다")
    void validateProduct_BlankInput_ShouldThrow() {
        String blankInput = "";

        Assertions.assertThatThrownBy(() -> InputValidator.validateProduct(blankInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.BLANK_INPUT.getMessage());
    }

    @Test
    @DisplayName("상품 입력이 null일 경우 예외를 발생시킨다")
    void validateProduct_NullInput_ShouldThrow() {
        String nullInput = null;

        Assertions.assertThatThrownBy(() -> InputValidator.validateProduct(nullInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.BLANK_INPUT.getMessage());
    }

    @Test
    @DisplayName("상품 입력 형식이 잘못된 경우 예외를 발생시킨다")
    void validateProduct_InvalidPattern_ShouldThrow() {
        String invalidInput = "콜라-10";

        Assertions.assertThatThrownBy(() -> InputValidator.validateProduct(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.INVALID_PRODUCT_INPUT.getMessage());
    }

    @Test
    @DisplayName("유효한 Y/N 입력을 검증한다")
    void validateYesOrNo_ValidInput_ShouldNotThrow() {
        String validYes = "Y";
        String validNo = "N";

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatCode(() -> InputValidator.validateYesOrNo(validYes)).doesNotThrowAnyException();
            softly.assertThatCode(() -> InputValidator.validateYesOrNo(validNo)).doesNotThrowAnyException();
        });
    }

    @Test
    @DisplayName("Y/N 입력이 비어있을 경우 예외를 발생시킨다")
    void validateYesOrNo_BlankInput_ShouldThrow() {
        String blankInput = "";

        Assertions.assertThatThrownBy(() -> InputValidator.validateYesOrNo(blankInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.BLANK_INPUT.getMessage());
    }

    @Test
    @DisplayName("Y/N 입력이 null일 경우 예외를 발생시킨다")
    void validateYesOrNo_NullInput_ShouldThrow() {
        String nullInput = null;

        Assertions.assertThatThrownBy(() -> InputValidator.validateYesOrNo(nullInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.BLANK_INPUT.getMessage());
    }

    @Test
    @DisplayName("Y/N 입력 형식이 잘못된 경우 예외를 발생시킨다")
    void validateYesOrNo_InvalidInput_ShouldThrow() {
        String invalidInput = "A";

        Assertions.assertThatThrownBy(() -> InputValidator.validateYesOrNo(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.INVALID_INPUT.getMessage());
    }
}
