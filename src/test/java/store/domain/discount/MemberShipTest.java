package store.domain.discount;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.order.NormalOrderItem;
import store.domain.order.Order;
import store.domain.order.PromotionOrderItem;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

class MemberShipTest {
    private MemberShip memberShip;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        memberShip = new MemberShip();
        promotion = new Promotion("Sample Promotion", 2, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(10));
    }

    @ParameterizedTest
    @CsvSource({
            "10000, 3000",
            "5000, 1500",
            "8000, 2400"
    })
    @DisplayName("30% 할인 금액을 정확히 계산한다.")
    void apply30PercentDiscount(int price, int expectedDiscount) {
        NormalProduct product = createNormalProduct("NonPromoItem", price);
        NormalOrderItem orderProduct = new NormalOrderItem(product, 1);
        Order order = new Order(List.of(orderProduct));

        int discount = memberShip.applyDiscount(order);

        Assertions.assertThat(discount).isEqualTo(expectedDiscount);
    }

    @Test
    @DisplayName("할인 금액이 0원일 때 동작을 검증한다.")
    void applyZeroDiscount_ShouldReturnZero() {
        NormalProduct product = createNormalProduct("ZeroPriceItem", 0);
        NormalOrderItem orderProduct = new NormalOrderItem(product, 1);
        Order order = new Order(List.of(orderProduct));

        int discount = memberShip.applyDiscount(order);

        Assertions.assertThat(discount).isZero();
    }

    @Test
    @DisplayName("할인 금액이 남은 한도를 초과하면 최대 한도까지만 할인한다.")
    void applyDiscount_ShouldLimitToMaxDiscount() {
        NormalProduct expensiveProduct = createNormalProduct("ExpensiveItem", 26667);
        NormalOrderItem orderProduct = new NormalOrderItem(expensiveProduct, 1);
        Order order = new Order(List.of(orderProduct));

        int discount = memberShip.applyDiscount(order);

        Assertions.assertThat(discount).isEqualTo(8000); // 최대 할인 한도
    }

    @Test
    @DisplayName("남은 할인 한도를 초과하지 않도록 처리한다.")
    void applyDiscount_ShouldNotExceedRemainingLimit() {
        NormalProduct product1 = createNormalProduct("Item1", 10000);
        NormalProduct expensiveProduct = createNormalProduct("Expensive", 26667);
        NormalOrderItem orderProduct1 = new NormalOrderItem(product1, 1);
        NormalOrderItem orderProduct2 = new NormalOrderItem(expensiveProduct, 1);

        Order order1 = new Order(List.of(orderProduct1));
        int discount1 = memberShip.applyDiscount(order1);

        Order order2 = new Order(List.of(orderProduct2));
        int discount2 = memberShip.applyDiscount(order2);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(discount1).isEqualTo(3000);
            softly.assertThat(discount2).isEqualTo(5000); // 남은 한도
        });
    }

    @Test
    @DisplayName("프로모션 적용된 상품은 할인 대상에서 제외한다.")
    void applyDiscount_ShouldExcludePromotionalItems() {
        NormalProduct normalProduct = createNormalProduct("PromoItem", 1000);
        PromotionProduct promotionalProduct = createPromotionProduct(promotion);

        PromotionOrderItem promotionOrderProduct = new PromotionOrderItem(normalProduct, promotionalProduct, 20, LocalDateTime.now());

        NormalProduct nonPromotionalProduct = createNormalProduct("NonPromoItem", 1000);
        NormalOrderItem normalOrderProduct = new NormalOrderItem(nonPromotionalProduct, 10);

        Order order = new Order(List.of(promotionOrderProduct, normalOrderProduct));

        int discount = memberShip.applyDiscount(order);

        Assertions.assertThat(discount).isEqualTo(6000); // 일반 상품에만 할인 적용
    }

    @Test
    @DisplayName("모든 상품이 프로모션 대상일 때 할인 금액은 0원이다.")
    void applyDiscount_ShouldReturnZeroWhenAllItemsArePromotional() {
        NormalProduct normalProduct = createNormalProduct("PromoItem", 1000);
        PromotionProduct promotionalProduct = createPromotionProduct(promotion);

        PromotionOrderItem promotionOrderProduct = new PromotionOrderItem(normalProduct, promotionalProduct, 10, LocalDateTime.now());

        Order order = new Order(List.of(promotionOrderProduct));

        int discount = memberShip.applyDiscount(order);

        Assertions.assertThat(discount).isZero();
    }

    private NormalProduct createNormalProduct(String name, int price) {
        return new NormalProduct(name, price, 10);
    }

    private PromotionProduct createPromotionProduct(Promotion promotion) {
        return new PromotionProduct("PromoItem", 1000, 10, promotion);
    }
}
