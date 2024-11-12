package store.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.domain.DomainErrorMessage;

class NormalProductTest {

    @Nested
    @DisplayName("toString 메서드 테스트")
    class ToStringTests {
        @Test
        @DisplayName("프로모션이 없는 상품을 올바르게 포맷팅한다")
        void testToStringWithoutPromotion() {
            NormalProduct product = new NormalProduct("사이다", 1200, 5);

            String actual = product.toString();

            String expected = "- 사이다 1,200원 5개";
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("재고가 없는 상품을 올바르게 포맷팅한다")
        void testToStringOutOfStock() {
            NormalProduct product = new NormalProduct("오렌지주스", 1800, 0);

            String actual = product.toString();

            String expected = "- 오렌지주스 1,800원 재고 없음";
            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("decrease 메서드 테스트")
    class DecreaseTests {
        @Test
        @DisplayName("주문 수량만큼 재고를 차감하고 잔여량이 없다")
        void shouldDecreaseStockCompletely() {
            NormalProduct product = new NormalProduct("콜라", 1000, 10);

            int remaining = product.reduceQuantity(10);

            Assertions.assertThat(product.getQuantity()).isZero();
            Assertions.assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("일반 상품은 재고보다 많은 수량을 주문하면 예외를 발생시킨다")
        void shouldThrowWhenOrderExceedsStock() {
            NormalProduct product = new NormalProduct("콜라", 1000, 5);

            Assertions.assertThatThrownBy(() -> product.reduceQuantity(10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }
}
