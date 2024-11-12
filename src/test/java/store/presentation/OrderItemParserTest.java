package store.presentation;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.application.OrderParser;
import store.application.UserOrder;

class OrderItemParserTest {
    private OrderParser orderParser;

    @BeforeEach
    void setUp() {
        orderParser = new OrderParser();
    }

    @Test
    @DisplayName("정상적인 입력을 파싱하여 OrderRequestDto 리스트를 반환한다")
    void parseInput_ValidInput_ShouldReturnOrderRequests() {
        String input = "[콜라-10],[사이다-5]";

        List<UserOrder> orderRequests = orderParser.parseInput(input);

        Assertions.assertThat(orderRequests).hasSize(2).extracting(UserOrder::productName, UserOrder::quantity)
                .containsExactlyInAnyOrder(Assertions.tuple("콜라", 10), Assertions.tuple("사이다", 5));
    }

    @Test
    @DisplayName("중복된 상품명이 입력된 경우 예외를 발생시킨다")
    void parseInput_DuplicateProductNames_ShouldThrowException() {
        String input = "[콜라-10],[콜라-5]";

        Assertions.assertThatThrownBy(() -> orderParser.parseInput(input)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.DUPLICATED_PRODUCT_NAME.getMessage());
    }

    @Test
    @DisplayName("음수 또는 0이 포함된 수량의 입력일 경우 예외를 발생시킨다")
    void parseInput_InvalidQuantity_ShouldThrowException() {
        String input = "[콜라-0],[사이다--5]";

        Assertions.assertThatThrownBy(() -> orderParser.parseInput(input)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.INVALID_QUANTITY.getMessage());
    }

    @Test
    @DisplayName("수량이 숫자가 아닌 경우 예외를 발생시킨다")
    void parseInput_NonNumericQuantity_ShouldThrowException() {
        String input = "[콜라-abc]";

        Assertions.assertThatThrownBy(() -> orderParser.parseInput(input)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.INVALID_INPUT.getMessage());
    }

    @Test
    @DisplayName("공백 입력일 경우 예외를 발생시킨다")
    void parseInput_BlankInput_ShouldThrowException() {
        String input = " ";

        Assertions.assertThatThrownBy(() -> orderParser.parseInput(input)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PresentationErrorMessage.INVALID_PRODUCT_INPUT.getMessage());
    }

    @Test
    @DisplayName("정상적인 입력을 파싱하여 공백을 제거하고 처리한다")
    void parseInput_ValidInputWithExtraSpaces_ShouldTrimAndParse() {
        String input = "[콜라 - 10], [사이다 - 5]"; // 공백 포함

        List<UserOrder> orderRequests = orderParser.parseInput(input);

        Assertions.assertThat(orderRequests).hasSize(2).extracting(UserOrder::productName, UserOrder::quantity)
                .containsExactlyInAnyOrder(Assertions.tuple("콜라", 10), Assertions.tuple("사이다", 5));
    }
}
