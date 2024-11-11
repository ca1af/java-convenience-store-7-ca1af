package store.domain.order;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.domain.DomainErrorMessage;
import store.domain.discount.Promotion;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

class PromotionOrderProductTest {
    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1));
    private final NormalProduct normalProduct = new NormalProduct("foo", 1000, 5);
    private final PromotionProduct promoProductOnePlusOne = new PromotionProduct("foo", 1000, 10, onePlusOne);
    private final PromotionProduct promoProductTwoPlusOne = new PromotionProduct("foo", 2000, 15, twoPlusOne);

    private PromotionOrderProduct createPromotionOrderProduct(NormalProduct normalProduct, PromotionProduct promoProduct, int orderQuantity) {
        return new PromotionOrderProduct(normalProduct, promoProduct, orderQuantity, LocalDateTime.now());
    }

    @Test
    @DisplayName("수량을 1 증가시킨다")
    void shouldIncreaseQuantityByOne() {
        PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 5);
        orderProduct.addQuantity();
        Assertions.assertThat(orderProduct.countFallbackToNormal()).isZero(); // 프로모션 재고 충분
        Assertions.assertThat(orderProduct.hasFallbackToNormal()).isFalse();
    }

    @Test
    @DisplayName("수량을 감소시킨다")
    void shouldSubtract() {
        PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 5);
        orderProduct.subtract(2);
        Assertions.assertThat(orderProduct.countFallbackToNormal()).isZero();
    }

    @Nested
    @DisplayName("validateDifferentProducts 메서드 테스트")
    class ValidateDifferentProductsTests {
        @Test
        @DisplayName("같은 상품으로 구성된 경우 예외를 발생시키지 않는다")
        void shouldNotThrowWhenProductsAreSame() {
            Assertions.assertThatCode(() -> createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 5))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("다른 상품이 섞여 있는 경우 예외를 발생시킨다")
        void shouldThrowWhenProductsAreDifferent() {
            PromotionProduct differentPromoProduct = new PromotionProduct("bar", 1000, 10, onePlusOne);

            Assertions.assertThatThrownBy(() -> createPromotionOrderProduct(normalProduct, differentPromoProduct, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    @Nested
    @DisplayName("hasFallbackToNormal 메서드 테스트")
    class HasFallbackToNormalTests {
        @Test
        @DisplayName("일반 재고를 사용해야 하는 경우 true를 반환한다")
        void shouldReturnTrueWhenFallbackToNormalIsNeeded() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 12);
            Assertions.assertThat(orderProduct.hasFallbackToNormal()).isTrue();
        }

        @Test
        @DisplayName("일반 재고가 필요하지 않은 경우 false를 반환한다")
        void shouldReturnFalseWhenFallbackToNormalIsNotNeeded() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 8);
            Assertions.assertThat(orderProduct.hasFallbackToNormal()).isFalse();
        }

        @Test
        @DisplayName("프로모션이 존재하며 상품 재고가 없으면 true를 반환한다")
        void promotionOutOfStock() {
            NormalProduct outOfStock = new NormalProduct("outOfStock", 1000, 10);
            PromotionProduct outOfStockPromoProduct = new PromotionProduct("outOfStock", 1000, 0, onePlusOne);
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(outOfStock, outOfStockPromoProduct, 10);

            boolean expected = orderProduct.hasFallbackToNormal();

            Assertions.assertThat(expected).isTrue();
        }
    }

    @Nested
    @DisplayName("getProductName 메서드 테스트")
    class GetProductNameTests {
        @Test
        @DisplayName("상품 이름을 반환한다")
        void shouldReturnProductName() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 5);
            Assertions.assertThat(orderProduct.getProductName()).isEqualTo("foo");
        }
    }

    @Nested
    @DisplayName("decreaseStocks 메서드 테스트")
    class DecreaseStocksTests {
        @Test
        @DisplayName("프로모션 상품 재고를 먼저 감소시킨다")
        void shouldDecreasePromotionStockFirst() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 5);
            orderProduct.decreaseStocks();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(promoProductOnePlusOne.getQuantity()).isEqualTo(5); // 10 - 5
                softly.assertThat(normalProduct.getQuantity()).isEqualTo(5); // 변경 없음
            });
        }

        @Test
        @DisplayName("프로모션 상품 재고가 부족하면 일반 재고를 감소시킨다")
        void shouldDecreaseNormalStockWhenPromotionStockIsInsufficient() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 12);
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
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 4);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(2);
        }

        @Test
        @DisplayName("주문 수량이 재고를 초과하면 프로모션 수량은 최대 재고만큼만 제공된다")
        void shouldLimitPromotedCountByStock() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 20);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(5);
        }

        @Test
        @DisplayName("주문 수량에 따른 프로모션 수량을 정확히 계산한다 (2+1 프로모션)")
        void shouldReturnCorrectPromotedCountForTwoPlusOne() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductTwoPlusOne, 4);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("2+1 프로모션에서 주문 수량이 부족하면 무료 제공 수량은 0이다")
        void shouldReturnZeroForInsufficientOrderQuantityInTwoPlusOne() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductTwoPlusOne, 1);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isZero();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("프로모션 조건을 만족하면 true를 반환한다")
        void shouldReturnTrueWhenUnclaimedFreeItemExists() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 3);

            boolean hasUnclaimedFreeItem = orderProduct.hasUnclaimedFreeItem();

            Assertions.assertThat(hasUnclaimedFreeItem).isTrue();
        }

        @Test
        @DisplayName("프로모션 기간이 아니면 false를 반환한다")
        void shouldReturnFalseWhenPromotionNotApplicable() {
            Promotion expiredPromotion = new Promotion("Expired Promotion", 1, 1, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
            PromotionProduct expiredPromoProduct = new PromotionProduct("foo", 1000, 10, expiredPromotion);
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, expiredPromoProduct, 3);

            boolean hasUnclaimedFreeItem = orderProduct.hasUnclaimedFreeItem();

            Assertions.assertThat(hasUnclaimedFreeItem).isFalse();
        }

        @Test
        @DisplayName("주문 수량이 프로모션 조건을 만족하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenOrderQuantityNotSatisfyPromotion() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductTwoPlusOne, 1);

            boolean hasUnclaimedFreeItem = orderProduct.hasUnclaimedFreeItem();

            Assertions.assertThat(hasUnclaimedFreeItem).isFalse();
        }
    }

    @Nested
    @DisplayName("hasEnoughStock 메서드 테스트")
    class HasEnoughStockTests {
        @Test
        @DisplayName("재고가 충분하면 true를 반환한다")
        void shouldReturnTrueWhenStockIsEnough() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 15);

            boolean hasEnoughStock = orderProduct.hasEnoughStock();

            Assertions.assertThat(hasEnoughStock).isTrue();
        }

        @Test
        @DisplayName("재고가 부족하면 false를 반환한다")
        void shouldReturnFalseWhenStockIsNotEnough() {
            PromotionOrderProduct orderProduct = createPromotionOrderProduct(normalProduct, promoProductOnePlusOne, 20);

            boolean hasEnoughStock = orderProduct.hasEnoughStock();

            Assertions.assertThat(hasEnoughStock).isFalse();
        }
    }
}
