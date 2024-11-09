package store.domain;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OrderTest {
    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Product normalProduct = new Product("foo", 1000, 5, null);
    private final Product promoProductOnePlusOne = new Product("foo", 1000, 10, onePlusOne);
    private final Product promoProductTwoPlusOne = new Product("foo", 2000, 15, twoPlusOne);
    private final OrderProducts orderProductsOnePlusOne = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));
    private final OrderProducts orderProductsTwoPlusOne = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

    @Nested
    @DisplayName("available 메서드 테스트")
    class AvailableTests {
        @Test
        @DisplayName("재고가 주문 수량 이상일 때 true를 반환한다")
        void shouldReturnTrueWhenStockIsSufficient() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 10);

            // when
            boolean available = order.available();

            // then
            Assertions.assertThat(available).isTrue();
        }

        @Test
        @DisplayName("재고가 주문 수량 미만일 때 false를 반환한다")
        void shouldReturnFalseWhenStockIsInsufficient() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 20);

            // when
            boolean available = order.available();

            // then
            Assertions.assertThat(available).isFalse();
        }

        @Test
        @DisplayName("경계값 - 재고와 주문량이 동일하면 true를 반환한다")
        void shouldReturnTrueWhenStockEqualsQuantity() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 15);

            // when
            boolean available = order.available();

            // then
            Assertions.assertThat(available).isTrue();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("프로모션 재고로 주문이 충족 가능하고 여분이 무료라면 true를 반환한다")
        void shouldReturnTrueWhenPromotionStockIsSufficient() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 9);

            // when
            boolean hasUnclaimedFreeItem = order.hasUnclaimedFreeItem();

            // then
            Assertions.assertThat(hasUnclaimedFreeItem).isTrue();
        }

        @Test
        @DisplayName("프로모션 재고로 주문이 충족 불가능한 경우 false를 반환한다")
        void shouldReturnFalseWhenPromotionStockIsInsufficient() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 15);

            // when
            boolean hasUnclaimedFreeItem = order.hasUnclaimedFreeItem();

            // then
            Assertions.assertThat(hasUnclaimedFreeItem).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 무료 아이템 여분이 없다면 false를 반환한다")
        void shouldReturnFalseWhenNoFreeItemsForTwoPlusOne() {
            // given
            Order order = new Order(orderProductsTwoPlusOne, 7);

            // when
            boolean hasUnclaimedFreeItem = order.hasUnclaimedFreeItem();

            // then
            Assertions.assertThat(hasUnclaimedFreeItem).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 무료 아이템 여분이 있으면 true를 반환한다")
        void shouldReturnTrueWhenFreeItemsForTwoPlusOne() {
            // given
            Order order = new Order(orderProductsTwoPlusOne, 5);

            // when
            boolean hasUnclaimedFreeItem = order.hasUnclaimedFreeItem();

            // then
            Assertions.assertThat(hasUnclaimedFreeItem).isTrue();
        }
    }

    @Nested
    @DisplayName("countFallbackToNormal 메서드 테스트")
    class CountFallbackToNormalTests {
        @Test
        @DisplayName("일반 재고를 사용해야 하는 수량을 정확히 계산한다")
        void shouldReturnCorrectFallbackQuantity() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 12);

            // when
            int fallbackQuantity = order.countFallbackToNormal();

            // then
            Assertions.assertThat(fallbackQuantity).isEqualTo(2);
        }

        @Test
        @DisplayName("프로모션 재고로 주문이 모두 충족 가능한 경우 일반 재고 사용 수량은 0이다")
        void shouldReturnZeroWhenPromotionStockIsSufficient() {
            // given
            Order order = new Order(orderProductsOnePlusOne, 8);

            // when
            int fallbackQuantity = order.countFallbackToNormal();

            // then
            Assertions.assertThat(fallbackQuantity).isZero();
        }

        @Test
        @DisplayName("경계값 - 일반 재고 부족 시 남은 수량 계산")
        void shouldReturnFallbackQuantityWhenPromotionStockIsInsufficient() {
            // given
            Order order = new Order(orderProductsTwoPlusOne, 18);

            // when
            int fallbackQuantity = order.countFallbackToNormal();

            // then
            Assertions.assertThat(fallbackQuantity).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidationTests {
        @Test
        @DisplayName("주문 수량이 음수일 때 예외를 발생시킨다")
        void shouldThrowWhenQuantityIsNegative() {
            // when / then
            Assertions.assertThatThrownBy(() -> new Order(orderProductsOnePlusOne, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("주문 수량이 0일 때 예외를 발생시킨다")
        void shouldThrowWhenQuantityIsZero() {
            // when / then
            Assertions.assertThatThrownBy(() -> new Order(orderProductsOnePlusOne, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("유효한 주문 수량이면 생성이 성공한다")
        void shouldCreateOrderWhenQuantityIsValid() {
            // when
            Order order = new Order(orderProductsOnePlusOne, 5);

            // then
            Assertions.assertThat(order).isNotNull();
        }
    }

    @Test
    @DisplayName("일반 상품 가격을 정확히 계산한다")
    void shouldReturnCorrectPrice() {
        // given
        Order order = new Order(orderProductsOnePlusOne, 12);

        // when
        int normalPrice = order.getNormalProductPrice();

        // then
        Assertions.assertThat(normalPrice).isEqualTo(1000 * 2); // 일반 재고에서 2개 사용
    }

    @Nested
    @DisplayName("getPromotedCount 메서드 테스트")
    class GetPromotedCountTests {
        @ParameterizedTest
        @CsvSource({"6, 2", // 2+1 프로모션, 6개 주문 -> 2번 프로모션 적용
                "7, 2", // 2+1 프로모션, 7개 주문 -> 2번 프로모션 적용 (1개 남음)
                "9, 3", // 2+1 프로모션, 9개 주문 -> 3번 프로모션 적용
                "10, 3", // 2+1 프로모션, 10개 주문 -> 3번 프로모션 적용 (1개 남음)
                "15, 5" // 2+1 프로모션, 15개 주문 -> 5번 프로모션 적용
        })
        @DisplayName("프로모션 재고가 충분한 경우 할인 적용 수량을 정확히 반환한다")
        void shouldReturnCorrectPromotedCountWhenPromotionStockIsSufficient(int orderQuantity,
                                                                            int expectedPromotedCount) {
            // given
            Order order = new Order(orderProductsTwoPlusOne, orderQuantity);

            // when
            int promotedCount = order.getPromotedCount();

            // then
            Assertions.assertThat(promotedCount).isEqualTo(expectedPromotedCount);
        }

        @Test
        @DisplayName("프로모션 재고가 부족한 경우 사용 가능한 최대 수량에 대해 할인 적용")
        void shouldReturnPromotedCountWhenPromotionStockIsInsufficient() {
            // given
            OrderProducts limitedStockProducts = new OrderProducts(List.of(new Product("foo", 2000, 5, twoPlusOne)));
            Order order = new Order(limitedStockProducts, 10);

            // when
            int promotedCount = order.getPromotedCount();

            // then
            Assertions.assertThat(promotedCount).isEqualTo(1); // 5 / (2 + 1) = 1번 프로모션 적용
        }

        @Test
        @DisplayName("프로모션 상품이 없는 경우 할인 적용 수량은 0이다")
        void shouldReturnZeroWhenNoPromotionExists() {
            // given
            Order order = new Order(new OrderProducts(List.of(normalProduct)), 10);

            // when
            int promotedCount = order.getPromotedCount();

            // then
            Assertions.assertThat(promotedCount).isZero();
        }
    }
}
