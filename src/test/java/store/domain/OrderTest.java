package store.domain;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTest {
    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Product normalProduct = new Product("foo", 1000, 5, null);
    private final Product promoProductOnePlusOne = new Product("foo", 1000, 10, onePlusOne);
    private final Product promoProductTwoPlusOne = new Product("foo", 2000, 15, twoPlusOne);
    private final OrderProducts orderProductsOnePlusOne = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));
    private final OrderProducts orderProductsTwoPlusOne = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

    @Nested
    @DisplayName("available 메서드")
    class AvailableTests {
        @Test
        @DisplayName("재고가 주문 수량 이상일 때 true를 반환한다")
        void shouldReturnTrueWhenStockIsSufficient() {
            Order order = new Order(orderProductsOnePlusOne, 10);
            Assertions.assertThat(order.available()).isTrue();
        }

        @Test
        @DisplayName("재고가 주문 수량 미만일 때 false를 반환한다")
        void shouldReturnFalseWhenStockIsInsufficient() {
            Order order = new Order(orderProductsOnePlusOne, 20);
            Assertions.assertThat(order.available()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("프로모션 재고로 주문이 충족 가능하고 여분이 무료라면 true를 반환한다")
        void shouldReturnTrueWhenPromotionStockIsSufficient() {
            Order order = new Order(orderProductsOnePlusOne, 9);
            Assertions.assertThat(order.hasUnclaimedFreeItem()).isTrue();
        }

        @Test
        @DisplayName("프로모션 재고로 주문이 충족 불가능한 경우 false를 반환한다")
        void shouldReturnFalseWhenPromotionStockIsInsufficient() {
            Order order = new Order(orderProductsOnePlusOne, 15);
            Assertions.assertThat(order.hasUnclaimedFreeItem()).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 무료 아이템 여분이 없다면 false를 반환한다")
        void shouldReturnFalseWhenNoFreeItemsForTwoPlusOne() {
            Order order = new Order(orderProductsTwoPlusOne, 7); // 7 % (2 + 1) == 1 -> 무료 없음
            Assertions.assertThat(order.hasUnclaimedFreeItem()).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 무료 아이템 여분이 있으면 true를 반환한다")
        void shouldReturnTrueWhenFreeItemsForTwoPlusOne() {
            Order order = new Order(orderProductsTwoPlusOne, 5); // 5 % (2 + 1) == 2 -> 무료 1개
            Assertions.assertThat(order.hasUnclaimedFreeItem()).isTrue();
        }
    }

    @Nested
    @DisplayName("countFallbackToNormal 메서드")
    class CountFallbackToNormalTests {
        @Test
        @DisplayName("일반 재고를 사용해야 하는 수량을 정확히 계산한다")
        void shouldReturnCorrectFallbackQuantity() {
            Order order = new Order(orderProductsOnePlusOne, 12);
            int fallbackQuantity = order.countFallbackToNormal();
            Assertions.assertThat(fallbackQuantity).isEqualTo(2);
        }

        @Test
        @DisplayName("프로모션 재고로 주문이 모두 충족 가능한 경우 일반 재고 사용 수량은 0이다")
        void shouldReturnZeroWhenPromotionStockIsSufficient() {
            Order order = new Order(orderProductsOnePlusOne, 8);
            int fallbackQuantity = order.countFallbackToNormal();
            Assertions.assertThat(fallbackQuantity).isZero();
        }
    }

    @Test
    @DisplayName("일반 상품 가격을 정확히 계산한다")
    void shouldReturnCorrectPrice() {
        Order order = new Order(orderProductsOnePlusOne, 12);
        int normalPrice = order.getNormalProductPrice();
        Assertions.assertThat(normalPrice).isEqualTo(1000 * 2); // 일반 재고에서 2개 사용
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidationTests {
        @Test
        @DisplayName("주문 수량이 음수일 때 예외를 발생시킨다")
        void shouldThrowWhenQuantityIsNegative() {
            Assertions.assertThatThrownBy(() -> new Order(orderProductsOnePlusOne, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("주문 수량이 0일 때 예외를 발생시킨다")
        void shouldThrowWhenQuantityIsZero() {
            Assertions.assertThatThrownBy(() -> new Order(orderProductsOnePlusOne, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }
}
