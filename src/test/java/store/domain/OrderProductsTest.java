package store.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("OrderProducts 클래스 테스트")
class OrderProductsTest {

    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Product normalProduct = new Product("콜라", 1000, 10, null);
    private final Product promoProductOnePlusOne = new Product("콜라", 1000, 5, onePlusOne);
    private final Product promoProductTwoPlusOne = new Product("콜라", 2000, 8, twoPlusOne);

    @Nested
    @DisplayName("생성자 검증")
    class ConstructorValidationTests {
        @Test
        @DisplayName("빈 리스트가 주어지면 예외를 발생시킨다")
        void shouldThrowWhenEmptyListIsProvided() {
            List<Product> emptyList = List.of();
            assertThatThrownBy(() -> new OrderProducts(emptyList))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        @Test
        @DisplayName("다른 상품이 섞여 있으면 예외를 발생시킨다")
        void shouldThrowWhenDifferentProductsAreProvided() {
            Product differentProduct = new Product("사이다", 1200, 5, null);
            List<Product> orderProducts = List.of(normalProduct, differentProduct);

            assertThatThrownBy(() -> new OrderProducts(orderProducts))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    @Nested
    @DisplayName("프로모션 및 일반 상품 테스트")
    class StockTests {
        @Test
        @DisplayName("getMaxCount는 모든 재고의 합계를 반환한다")
        void getMaxCount_ShouldReturnSumOfAllStocks() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            int maxCount = orderProducts.getMaxCount();

            Assertions.assertThat(maxCount).isEqualTo(15);
        }

        @Test
        @DisplayName("getPromotionStock는 프로모션 상품 재고만 합산한다")
        void getPromotionStock_ShouldReturnSumOfPromotionStock() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            int promotionStock = orderProducts.getPromotionStock();

            Assertions.assertThat(promotionStock).isEqualTo(5);
        }

        @Test
        @DisplayName("getNormalProductPrice는 일반 상품 가격을 정확히 계산한다")
        void getNormalProductPrice_ShouldReturnCorrectPrice() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            int normalPrice = orderProducts.getNormalProductPrice(3);

            Assertions.assertThat(normalPrice).isEqualTo(3000); // 3 * 1000
        }
    }

    @Nested
    @DisplayName("1+1 재고 차감 메서드 테스트")
    class OnePlusOneDecreaseTests {
        @Test
        @DisplayName("decrease 메서드는 프로모션 재고를 먼저 차감한다")
        void decrease_ShouldReducePromotionStockFirst() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            orderProducts.decrease(4);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(1); // 5 - 4
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(10); // 바뀌지않음
            });
        }

        @Test
        @DisplayName("decrease 메서드는 프로모션 재고가 부족하면 일반 재고를 차감한다")
        void decrease_ShouldFallbackToNormalStockWhenPromotionStockIsInsufficient() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            orderProducts.decrease(7);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(0); // 5 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(8); // 10 - 2
            });
        }

        @Test
        @DisplayName("decrease 메서드는 일반 재고와 프로모션 재고를 모두 소진할 수 있다")
        void decrease_ShouldConsumeAllStocks() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            orderProducts.decrease(15);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(0); // 5 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(0); // 10 - 10
            });
        }

        @Test
        @DisplayName("재고가 부족하면 decrease는 잔여량을 처리하지 못한다")
        void decrease_ShouldNotExceedStockLimits() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            orderProducts.decrease(20);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(0); // 5 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(0); // 10 - 10
            });
        }
    }

    @Nested
    @DisplayName("2+1 프로모션 재고 차감 테스트")
    class TwoPlusOneDecreaseTests {

        @Test
        @DisplayName("프로모션 재고를 정확히 차감한다")
        void decrease_ShouldReduceTwoPlusOnePromotionStock() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            orderProducts.decrease(6);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(2); // 8 - 6
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(10); // unchanged
            });
        }

        @Test
        @DisplayName("프로모션 재고가 부족하면 일반 재고를 사용한다")
        void decrease_ShouldFallbackToNormalStockWhenTwoPlusOnePromotionStockIsInsufficient() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            orderProducts.decrease(10);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(0); // 8 - 8
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(8); // 10 - 2
            });
        }

        @Test
        @DisplayName("2+1 프로모션으로 모든 주문을 처리한다")
        void decrease_ShouldHandleExactTwoPlusOnePromotion() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne));

            orderProducts.decrease(6);

            Assertions.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(2); // 8 - 6
        }

        @Test
        @DisplayName("2+1 프로모션과 일반 재고를 모두 소진한다")
        void decrease_ShouldConsumeAllTwoPlusOnePromotionAndNormalStock() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            orderProducts.decrease(18);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(0); // 8 - 8
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(0); // 10 - 10
            });
        }

        @Test
        @DisplayName("2+1 프로모션이 부족하면 잔여량은 그대로 남는다")
        void decrease_ShouldLeaveUnprocessedWhenTwoPlusOneAndNormalStockAreInsufficient() {
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            orderProducts.decrease(25); // Only 18 available

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(0); // 8 - 8
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(0); // 10 - 10
            });
        }
    }

}
