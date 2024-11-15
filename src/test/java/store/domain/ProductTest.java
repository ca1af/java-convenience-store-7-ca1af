package store.domain;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProductTest {

    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(10));
    private final Promotion twoPlusOne = new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(10));

    @Nested
    @DisplayName("toString 메서드 테스트")
    class ToStringTests {
        @Test
        @DisplayName("프로모션이 있는 상품을 올바르게 포맷팅한다")
        void testToStringWithPromotion() {
            Product product = new Product("콜라", 1000, 10, onePlusOne);

            String actual = product.toString();

            String expected = "- 콜라 1,000원 10개 1+1";
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("프로모션이 없는 상품을 올바르게 포맷팅한다")
        void testToStringWithoutPromotion() {
            Product product = new Product("사이다", 1200, 5, null);

            String actual = product.toString();

            String expected = "- 사이다 1,200원 5개";
            Assertions.assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("재고가 없는 상품을 올바르게 포맷팅한다")
        void testToStringOutOfStock() {
            Product product = new Product("오렌지주스", 1800, 0, null);

            String actual = product.toString();

            String expected = "- 오렌지주스 1,800원 재고 없음";
            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("promotionExists 메서드 테스트")
    class PromotionExistsTests {
        @Test
        @DisplayName("프로모션이 있는 경우 true를 반환한다")
        void testPromotionExistsTrue() {
            Product product = new Product("감자칩", 1500, 8, onePlusOne);

            Assertions.assertThat(product.promotionExists(DateTimes.now())).isTrue();
        }

        @Test
        @DisplayName("프로모션이 없는 경우 false를 반환한다")
        void testPromotionExistsFalse() {
            Product product = new Product("초코바", 1200, 5, null);

            Assertions.assertThat(product.promotionExists(DateTimes.now())).isFalse();
        }
    }

    @Nested
    @DisplayName("hasUnclaimedFreeItem 메서드 테스트")
    class HasUnclaimedFreeItemTests {
        @Test
        @DisplayName("조건에 맞는 경우 true를 반환한다")
        void shouldReturnTrueWhenEligibleForFreeItem() {
            Product product = new Product("콜라", 1000, 10, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(3, DateTimes.now())).isTrue();
        }

        @Test
        @DisplayName("조건에 맞지 않는 경우 false를 반환한다")
        void shouldReturnFalseWhenNotEligibleForFreeItem() {
            Product product = new Product("콜라", 1000, 5, onePlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(5, DateTimes.now())).isFalse();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 조건 충족 시 true 반환")
        void shouldReturnTrueWhenEligibleForTwoPlusOne() {
            Product product = new Product("콜라", 1000, 10, twoPlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(5, DateTimes.now())).isTrue();
        }

        @Test
        @DisplayName("2+1 프로모션 테스트 - 조건 미충족 시 false 반환")
        void shouldReturnFalseWhenNotEligibleForTwoPlusOne() {
            Product product = new Product("콜라", 1000, 8, twoPlusOne);

            Assertions.assertThat(product.hasUnclaimedFreeItem(6, DateTimes.now())).isFalse();
        }
    }

    @Nested
    @DisplayName("decrease 메서드 테스트")
    class DecreaseTests {
        @Test
        @DisplayName("주문 수량만큼 재고를 차감하고 잔여량이 없다")
        void shouldDecreaseStockCompletely() {
            Product product = new Product("콜라", 1000, 10, null);

            int remaining = product.decrease(10);

            Assertions.assertThat(product.getQuantity()).isZero();
            Assertions.assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("재고보다 많은 수량을 주문하면 재고를 모두 소진하고 잔여량을 반환한다")
        void shouldReturnRemainingWhenStockIsInsufficient() {
            Product product = new Product("콜라", 1000, 5, null);

            int remaining = product.decrease(10);

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
            Product product = new Product("콜라", 1000, 10, twoPlusOne);

            int promotedCount = product.getPromotedCount(6);

            Assertions.assertThat(promotedCount).isEqualTo(2); // 6 주문 -> 4 구매 + 2 무료
        }

        @Test
        @DisplayName("재고보다 주문 수량이 많을 경우 최대 가능한 무료 상품 개수를 반환한다")
        void shouldCapPromotedCountByStock() {
            Product product = new Product("콜라", 1000, 5, twoPlusOne);

            int promotedCount = product.getPromotedCount(8);

            Assertions.assertThat(promotedCount).isEqualTo(1);
        }
    }
}
