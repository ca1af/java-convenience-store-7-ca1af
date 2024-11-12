package store.domain.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.domain.DomainErrorMessage;
import store.domain.discount.Promotion;

class PromotionProductTest {

    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(10));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(10));

    @Test
    @DisplayName("프로모션이 없는 상품은 예외가 발생한다")
    void promotionIsEmpty() {
        assertThatThrownBy(() -> new PromotionProduct("foo", 1000, 5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.PROMOTION_CANNOT_BE_NULL.getMessage());
    }

    @Nested
    @DisplayName("toString 메서드 테스트")
    class ToStringTests {
        @Test
        @DisplayName("프로모션이 있는 상품을 올바르게 포맷팅한다")
        void testToStringWithPromotion() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 10, onePlusOne);

            String actual = product.toString();

            String expected = "- 콜라 1,000원 10개 1+1";
            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("promotionExists 메서드 테스트")
    class PromotionExistsTests {
        @Test
        @DisplayName("프로모션 기간 내에 있으면 true를 반환한다")
        void testPromotionExistsTrue() {
            PromotionProduct product = new PromotionProduct("감자칩", 1500, 8, onePlusOne);

            Assertions.assertThat(product.promotionExists(LocalDateTime.now())).isTrue();
        }

        @Test
        @DisplayName("프로모션 기간이 지나면 false를 반환한다")
        void testPromotionExistsFalse() {
            Promotion expiredPromotion = new Promotion("Expired", 1, 1, LocalDateTime.now().minusDays(10),
                    LocalDateTime.now().minusDays(5));
            PromotionProduct product = new PromotionProduct("초코바", 1200, 5, expiredPromotion);

            Assertions.assertThat(product.promotionExists(LocalDateTime.now())).isFalse();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("조건에 맞는 경우 true를 반환한다")
        void shouldReturnTrueWhenEligibleForFreeItem() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 10, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(3, LocalDateTime.now())).isTrue();
        }

        @Test
        @DisplayName("재고가 초과할 경우 false를 반환한다")
        void hasUnclaimedFreeItem_stockExceeded() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 5, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(5, LocalDateTime.now())).isFalse();
        }

        @Test
        @DisplayName("주문량이 0인 경우 false를 반환한다")
        void hasUnclaimedFreeItem_zeroQuantity() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 5, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(0, LocalDateTime.now())).isFalse();
        }

        @Test
        @DisplayName("주문량이 0인 경우 false를 반환한다")
        void hasUnclaimedFreeItem_promotionExpired() {
            new Promotion("expired", 1, 1, LocalDateTime.now(), LocalDateTime.now());
            PromotionProduct product = new PromotionProduct("콜라", 1000, 5, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(0, LocalDateTime.now())).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 조건 충족 시 true 반환")
        void shouldReturnTrueWhenEligibleForTwoPlusOne() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 6, twoPlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(5, LocalDateTime.now())).isTrue();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 조건 미충족 시 false 반환")
        void shouldReturnFalseWhenNotEligibleForTwoPlusOne() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 8, twoPlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(6, LocalDateTime.now())).isFalse();
        }
    }

    @Nested
    @DisplayName("reduceQuantity 메서드 테스트")
    class ReduceQuantityTests {
        @Test
        @DisplayName("주문 수량만큼 재고를 차감하고 잔여량이 없다")
        void shouldDecreaseStockCompletely() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 10, onePlusOne);

            int remaining = product.reduceQuantity(10);

            Assertions.assertThat(product.getQuantity()).isZero();
            Assertions.assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("재고보다 많은 수량을 주문하면 재고를 모두 소진하고 잔여량을 반환한다")
        void shouldReturnRemainingWhenStockIsInsufficient() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 5, onePlusOne);

            int remaining = product.reduceQuantity(10);

            Assertions.assertThat(product.getQuantity()).isZero();
            Assertions.assertThat(remaining).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("getPromotedCount 메서드 테스트")
    class GetPromotedCountTests {
        @Test
        @DisplayName("프로모션이 적용되는 주문 수량에 대한 무료 상품 개수를 반환한다")
        void shouldReturnPromotedCount() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 10, twoPlusOne);

            int promotedCount = product.getPromotedCount(6);

            Assertions.assertThat(promotedCount).isEqualTo(2); // 6 주문 -> 4 구매 + 2 무료
        }

        @Test
        @DisplayName("재고보다 주문 수량이 많을 경우 최대 가능한 무료 상품 개수를 반환한다")
        void shouldCapPromotedCountByStock() {
            PromotionProduct product = new PromotionProduct("콜라", 1000, 5, twoPlusOne);

            int promotedCount = product.getPromotedCount(8);

            Assertions.assertThat(promotedCount).isEqualTo(1);
        }
    }
}
