package store.domain;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("toString 메서드가 프로모션이 있는 상품을 올바르게 포맷팅한다")
    void testToStringWithPromotion() {
        Promotion promotion = new Promotion("탄산2+1", 2, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        Product product = new Product("콜라", 1000, 10, promotion);

        String actual = product.toString();

        String expected = "- 콜라 1,000원 10개 탄산2+1";
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("toString 메서드가 프로모션이 없는 상품을 올바르게 포맷팅한다")
    void testToStringWithoutPromotion() {
        Product product = new Product("사이다", 1200, 5, null);

        String actual = product.toString();

        String expected = "- 사이다 1,200원 5개";
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("promotionExists 메서드가 프로모션 존재 여부를 올바르게 반환한다")
    void testPromotionExists() {
        Promotion promotion = new Promotion("반짝할인", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        Product productWithPromotion = new Product("감자칩", 1500, 8, promotion);
        Product productWithoutPromotion = new Product("초코바", 1200, 5, null);

        Assertions.assertThat(productWithPromotion.promotionExists()).isTrue();
        Assertions.assertThat(productWithoutPromotion.promotionExists()).isFalse();
    }

    @Test
    @DisplayName("getTotalPrice 메서드가 상품 총 가격을 정확히 계산한다")
    void testGetTotalPrice() {
        Product product = new Product("에너지바", 2000, 3, null);

        int totalPrice = product.getTotalPrice();

        Assertions.assertThat(totalPrice).isEqualTo(6000);
    }

    @Test
    @DisplayName("hasRemains 메서드가 프로모션 조건에 맞는 수량 여부를 반환한다")
    void testHasRemains() {
        Promotion promotion = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        Product product = new Product("콜라", 1000, 10, promotion);

        Assertions.assertThat(product.hasRemains(3)).isTrue();
    }
}
