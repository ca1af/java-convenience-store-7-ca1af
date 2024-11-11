package store.domain.order;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.domain.DomainErrorMessage;
import store.domain.product.NormalProduct;

class NormalOrderItemTest {

    private final NormalProduct normalProduct = new NormalProduct("StandardItem", 1000, 10);

    private NormalOrderItem createNormalOrderProduct(NormalProduct product, int orderQuantity) {
        return new NormalOrderItem(product, orderQuantity);
    }

    @Test
    @DisplayName("일반 상품이 포함되지 않으면 예외가 발생한다.")
    void validateNormalProductNull() {
        assertThatThrownBy(() ->  new NormalOrderItem(null, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.NORMAL_PRODUCT_NEEDED.getMessage());

    }

    @Test
    @DisplayName("수량을 1 증가시킨다")
    void shouldIncreaseQuantityByOne() {
        NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);
        orderProduct.addQuantity();

        Assertions.assertThat(orderProduct.getOrderQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("수량을 감소시킨다")
    void shouldSubtractQuantity() {
        NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);
        orderProduct.subtract(2);

        Assertions.assertThat(orderProduct.getOrderQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("일반 상품의 프로모션 재고는 항상 0이다")
    void getPromotionStock() {
        NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);
        Assertions.assertThat(orderProduct.getPromotionStock()).isZero();
    }

    @Nested
    @DisplayName("hasEnoughStock 메서드 테스트")
    class HasEnoughStockTests {
        @Test
        @DisplayName("재고가 충분하면 true를 반환한다")
        void shouldReturnTrueWhenStockIsEnough() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            boolean hasEnoughStock = orderProduct.hasEnoughStock();

            Assertions.assertThat(hasEnoughStock).isTrue();
        }

        @Test
        @DisplayName("재고가 부족하면 false를 반환한다")
        void shouldReturnFalseWhenStockIsNotEnough() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 15);

            boolean hasEnoughStock = orderProduct.hasEnoughStock();

            Assertions.assertThat(hasEnoughStock).isFalse();
        }
    }

    @Nested
    @DisplayName("decreaseStocks 메서드 테스트")
    class DecreaseStocksTests {
        @Test
        @DisplayName("재고를 정확히 감소시킨다")
        void shouldDecreaseStockCorrectly() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);
            orderProduct.decreaseStocks();

            Assertions.assertThat(normalProduct.getQuantity()).isEqualTo(5); // 10 - 5
        }

        @Test
        @DisplayName("주문 수량이 재고를 초과하면 예외를 발생시킨다")
        void shouldThrowExceptionWhenOrderQuantityExceedsStock() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 15);

            assertThatThrownBy(orderProduct::decreaseStocks)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }

    @Nested
    @DisplayName("getTotalPrice 메서드 테스트")
    class GetTotalPriceTests {
        @Test
        @DisplayName("총 가격을 정확히 계산한다")
        void shouldCalculateTotalPriceCorrectly() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            int totalPrice = orderProduct.getTotalPrice();

            Assertions.assertThat(totalPrice).isEqualTo(5000); // 1000 * 5
        }
    }

    @Nested
    @DisplayName("getProductName 메서드 테스트")
    class GetProductNameTests {
        @Test
        @DisplayName("상품 이름을 반환한다")
        void shouldReturnProductName() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            String productName = orderProduct.getProductName();

            Assertions.assertThat(productName).isEqualTo("StandardItem");
        }
    }

    @Nested
    @DisplayName("calculatePromotedCount 메서드 테스트")
    class CalculatePromotedCountTests {
        @Test
        @DisplayName("프로모션이 없으므로 0을 반환한다")
        void shouldReturnZeroAsNoPromotion() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            int promotedCount = orderProduct.calculatePromotedCount();

            Assertions.assertThat(promotedCount).isZero();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("프로모션이 없으므로 false를 반환한다")
        void shouldReturnFalseAsNoPromotion() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            boolean hasUnclaimedFreeItem = orderProduct.hasUnclaimedFreeItem();

            Assertions.assertThat(hasUnclaimedFreeItem).isFalse();
        }
    }

    @Nested
    @DisplayName("hasFallbackToNormal 메서드 테스트")
    class HasFallbackToNormalTests {
        @Test
        @DisplayName("프로모션이 없으므로 false를 반환한다")
        void shouldReturnFalseAsNoPromotion() {
            NormalOrderItem orderProduct = createNormalOrderProduct(normalProduct, 5);

            boolean hasFallbackToNormal = orderProduct.hasFallbackToNormal();

            Assertions.assertThat(hasFallbackToNormal).isFalse();
        }
    }
}
