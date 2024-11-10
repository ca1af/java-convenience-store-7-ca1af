package store.domain;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderProductTest {
    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1));
    private final Product normalProduct = new Product("foo", 1000, 5, null);
    private final Product promoProductOnePlusOne = new Product("foo", 1000, 10, onePlusOne);
    private final List<Product> stocksOnePlusOne = List.of(promoProductOnePlusOne, normalProduct);
    private final Product promoProductTwoPlusOne = new Product("foo", 2000, 15, twoPlusOne);
    private final List<Product> stocksTwoPlusOne = List.of(promoProductTwoPlusOne, normalProduct);

    private OrderProduct createOrder(List<Product> products, int orderQuantity) {
        return new OrderProduct(products, orderQuantity, DateTimes.now());
    }

    @Test
    @DisplayName("수량을 1 증가시킨다")
    void shouldIncreaseQuantityByOne() {
        OrderProduct orderProduct = createOrder(stocksOnePlusOne, 5);
        orderProduct.addQuantity();
        Assertions.assertThat(orderProduct.countFallbackToNormal()).isZero(); // 프로모션 재고 충분
        Assertions.assertThat(orderProduct.hasFallbackToNormal()).isFalse();
    }

    @Test
    @DisplayName("수량을 감소시킨다")
    void shouldSubtract() {
        OrderProduct orderProduct = createOrder(stocksOnePlusOne, 5);
        orderProduct.subtract(2);
        Assertions.assertThat(orderProduct.countFallbackToNormal()).isZero();
    }

    @Nested
    @DisplayName("validateDifferentProducts 메서드 테스트")
    class ValidateDifferentProductsTests {
        @Test
        @DisplayName("같은 상품으로 구성된 리스트는 예외를 발생시키지 않는다")
        void shouldNotThrowWhenProductsAreSame() {
            Assertions.assertThatCode(() -> createOrder(stocksOnePlusOne, 5)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("다른 상품이 섞여 있는 경우 예외를 발생시킨다")
        void shouldThrowWhenProductsAreDifferent() {
            List<Product> mixedStocks = List.of(normalProduct, new Product("bar", 1500, 10, null));

            Assertions.assertThatThrownBy(() -> createOrder(mixedStocks, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    @Nested
    @DisplayName("hasFallbackToNormal 메서드 테스트")
    class HasFallbackToNormal {
        @Test
        @DisplayName("프로모션이 없는 상품은 false 를 반환한다.")
        void promotionIsEmpty() {
            OrderProduct orderProduct = new OrderProduct(List.of(normalProduct), 10, DateTimes.now());

            boolean expected = orderProduct.hasFallbackToNormal();

            Assertions.assertThat(expected).isFalse();
        }

        @Test
        @DisplayName("프로모션이 존재하며 상품 재고가 없으면 true 를 반환한다.")
        void promotionOutOfStock() {
            Product outOfStock = new Product("outOfStock", 1000, 0, onePlusOne);
            OrderProduct orderProduct = new OrderProduct(List.of(outOfStock), 10, DateTimes.now());

            boolean expected = orderProduct.hasFallbackToNormal();

            Assertions.assertThat(expected).isTrue();
        }
    }

    @Nested
    @DisplayName("hasFallbackToNormal 메서드 테스트")
    class HasFallbackToNormalTests {
        @Test
        @DisplayName("일반 재고를 사용해야 하는 경우 true를 반환한다")
        void shouldReturnTrueWhenFallbackToNormalIsNeeded() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 12);
            Assertions.assertThat(orderProduct.hasFallbackToNormal()).isTrue();
        }

        @Test
        @DisplayName("일반 재고가 필요하지 않은 경우 false를 반환한다")
        void shouldReturnFalseWhenFallbackToNormalIsNotNeeded() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 8);
            Assertions.assertThat(orderProduct.hasFallbackToNormal()).isFalse();
        }
    }

    @Nested
    @DisplayName("getProductName 메서드 테스트")
    class GetProductNameTests {
        @Test
        @DisplayName("상품 이름을 반환한다")
        void shouldReturnProductName() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 5);
            Assertions.assertThat(orderProduct.getProductName()).isEqualTo("foo");
        }
    }

    @Nested
    @DisplayName("decreaseStocks 메서드 테스트")
    class DecreaseStocksTests {
        @Test
        @DisplayName("프로모션 상품 재고를 먼저 감소시킨다")
        void shouldDecreasePromotionStockFirst() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 5);
            orderProduct.decreaseStocks();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(5); // 10 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(5); // 변경 없음
            });
        }

        @Test
        @DisplayName("프로모션 상품 재고가 부족하면 일반 재고를 감소시킨다")
        void shouldDecreaseNormalStockWhenPromotionStockIsInsufficient() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 12);
            orderProduct.decreaseStocks();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(0); // 10 - 10
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(3); // 5 - 2
            });
        }
    }

    @Nested
    @DisplayName("프로모션 수량 계산 메서드 테스트")
    class GetPromotedCountTests {
        @Test
        @DisplayName("주문 수량에 따른 프로모션 수량을 정확히 계산한다 (1+1 프로모션)")
        void shouldReturnCorrectPromotedCountForOnePlusOne() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 4);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(2);
        }

        @Test
        @DisplayName("주문 수량이 재고를 초과하면 프로모션 수량은 최대 재고만큼만 제공된다")
        void shouldLimitPromotedCountByStock() {
            OrderProduct orderProduct = createOrder(stocksOnePlusOne, 20);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(5);
        }

        @Test
        @DisplayName("프로모션이 없는 상품일 경우 0을 반환한다")
        void shouldReturnZeroWhenNoPromotionExists() {
            OrderProduct orderProduct = createOrder(List.of(normalProduct), 10);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isZero();
        }

        @Test
        @DisplayName("주문 수량에 따른 프로모션 수량을 정확히 계산한다 (2+1 프로모션)")
        void shouldReturnCorrectPromotedCountForTwoPlusOne() {
            OrderProduct orderProduct = createOrder(stocksTwoPlusOne, 4);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("2+1 프로모션에서 주문 수량이 부족하면 무료 제공 수량은 0이다")
        void shouldReturnZeroForInsufficientOrderQuantityInTwoPlusOne() {
            OrderProduct orderProduct = createOrder(stocksTwoPlusOne, 1); // 주문 수량이 프로모션 조건 미달

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isZero();
        }
    }
}
