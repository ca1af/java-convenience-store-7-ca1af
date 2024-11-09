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
            // given
            List<Product> emptyList = List.of();

            // when / then
            assertThatThrownBy(() -> new OrderProducts(emptyList))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        @Test
        @DisplayName("다른 상품이 섞여 있으면 예외를 발생시킨다")
        void shouldThrowWhenDifferentProductsAreProvided() {
            // given
            Product differentProduct = new Product("사이다", 1200, 5, null);
            List<Product> orderProducts = List.of(normalProduct, differentProduct);

            // when / then
            assertThatThrownBy(() -> new OrderProducts(orderProducts))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    @Nested
    @DisplayName("재고 관련 메서드 테스트")
    class StockTests {
        @Test
        @DisplayName("getMaxCount는 모든 재고의 합계를 반환한다")
        void getMaxCount_ShouldReturnSumOfAllStocks() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            int maxCount = orderProducts.getMaxCount();

            // then
            Assertions.assertThat(maxCount).isEqualTo(15);
        }

        @Test
        @DisplayName("getPromotionStock는 프로모션 상품 재고만 합산한다")
        void getPromotionStock_ShouldReturnSumOfPromotionStock() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            int promotionStock = orderProducts.getPromotionStock();

            // then
            Assertions.assertThat(promotionStock).isEqualTo(5);
        }

        @Test
        @DisplayName("getNormalProductPrice는 일반 상품 가격을 정확히 계산한다")
        void getNormalProductPrice_ShouldReturnCorrectPrice() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            int normalPrice = orderProducts.getNormalProductPrice(3);

            // then
            Assertions.assertThat(normalPrice).isEqualTo(3000); // 3 * 1000
        }
    }

    @Nested
    @DisplayName("1+1 재고 차감 메서드 테스트")
    class OnePlusOneDecreaseTests {
        @Test
        @DisplayName("decrease 메서드는 프로모션 재고를 먼저 차감한다")
        void decrease_ShouldReducePromotionStockFirst() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            orderProducts.decrease(4);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(1); // 5 - 4
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(10); // unchanged
            });
        }

        @Test
        @DisplayName("decrease 메서드는 프로모션 재고가 부족하면 일반 재고를 차감한다")
        void decrease_ShouldFallbackToNormalStockWhenPromotionStockIsInsufficient() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            orderProducts.decrease(7);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(0); // 5 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(8); // 10 - 2
            });
        }

        @Test
        @DisplayName("decrease 메서드는 모든 재고를 소진할 수 있다")
        void decrease_ShouldConsumeAllStocks() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne, normalProduct));

            // when
            orderProducts.decrease(15);

            // then
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
        @DisplayName("decrease 메서드는 정확히 2+1 프로모션 재고를 차감한다")
        void decrease_ShouldReduceTwoPlusOnePromotionStock() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            // when
            orderProducts.decrease(6);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(2); // 8 - 6
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(10); // unchanged
            });
        }

        @Test
        @DisplayName("decrease 메서드는 2+1 프로모션 재고가 부족하면 일반 재고를 사용한다")
        void decrease_ShouldFallbackToNormalStockWhenTwoPlusOnePromotionStockIsInsufficient() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne, normalProduct));

            // when
            orderProducts.decrease(10);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductTwoPlusOne.getQuantity()).isEqualTo(0); // 8 - 8
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(8); // 10 - 2
            });
        }
    }

    @Nested
    @DisplayName("프로모션 수량 계산 메서드 테스트")
    class GetPromotedCountTests {

        @Test
        @DisplayName("주문 수량에 따른 프로모션 수량을 정확히 계산한다 (1+1 프로모션)")
        void shouldReturnCorrectPromotedCountForOnePlusOne() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne));

            // when
            int promotedCount = orderProducts.getPromotedCount(4);

            // then
            Assertions.assertThat(promotedCount).isEqualTo(2);
        }

        @Test
        @DisplayName("주문 수량이 재고를 초과하면 프로모션 수량은 최대 재고만큼만 제공된다")
        void shouldLimitPromotedCountByStock() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductOnePlusOne));

            // when
            int promotedCount = orderProducts.getPromotedCount(6); // 주문 수량이 재고보다 많음

            // then
            Assertions.assertThat(promotedCount).isEqualTo(5);
        }

        @Test
        @DisplayName("프로모션이 없는 상품일 경우 0을 반환한다")
        void shouldReturnZeroWhenNoPromotionExists() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(normalProduct));

            // when
            int promotedCount = orderProducts.getPromotedCount(10);

            // then
            Assertions.assertThat(promotedCount).isZero();
        }

        @Test
        @DisplayName("주문 수량에 따른 프로모션 수량을 정확히 계산한다 (2+1 프로모션)")
        void shouldReturnCorrectPromotedCountForTwoPlusOne() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne));

            // when
            int promotedCount = orderProducts.getPromotedCount(4);

            // then
            Assertions.assertThat(promotedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("2+1 프로모션에서 주문 수량이 부족하면 무료 제공 수량은 0이다")
        void shouldReturnZeroForInsufficientOrderQuantityInTwoPlusOne() {
            // given
            OrderProducts orderProducts = new OrderProducts(List.of(promoProductTwoPlusOne));

            // when
            int promotedCount = orderProducts.getPromotedCount(1); // 주문 수량이 프로모션 조건 미달

            // then
            Assertions.assertThat(promotedCount).isZero();
        }
    }

}
