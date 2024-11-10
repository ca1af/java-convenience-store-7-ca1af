package store.domain;

import camp.nextstep.edu.missionutils.DateTimes;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;

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
        Product product = createNonPromotionalProduct("NonPromoItem", price);
        Orders orders = new Orders(List.of(new Order(List.of(product), 1, DateTimes.now())));

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isEqualTo(expectedDiscount);
    }

    @Test
    @DisplayName("할인 금액이 0원일 때 동작을 검증한다.")
    void applyZeroDiscount_ShouldReturnZero() {
        Product product = createNonPromotionalProduct("ZeroPriceItem", 0);
        Orders orders = new Orders(List.of(new Order(List.of(product), 1, DateTimes.now())));

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isZero();
    }

    @Test
    @DisplayName("할인 금액이 남은 한도를 초과하면 최대 한도까지만 할인한다.")
    void applyDiscount_ShouldLimitToMaxDiscount() {
        Product expensiveProduct = createNonPromotionalProduct("ExpensiveItem", 26667);
        Orders orders = new Orders(List.of(new Order(List.of(expensiveProduct), 1, DateTimes.now())));

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isEqualTo(8000); // 최대 할인 한도
    }

    @Test
    @DisplayName("남은 할인 한도를 초과하지 않도록 처리한다.")
    void applyDiscount_ShouldNotExceedRemainingLimit() {
        Product product1 = createNonPromotionalProduct("Item1", 10000);
        Product expensiveProduct = createNonPromotionalProduct("Expensive", 26667);
        Orders orders1 = new Orders(List.of(new Order(List.of(product1), 1, DateTimes.now())));
        Orders orders2 = new Orders(List.of(new Order(List.of(expensiveProduct), 1, DateTimes.now())));

        int discount1 = memberShip.applyDiscount(orders1);
        int discount2 = memberShip.applyDiscount(orders2);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(discount1).isEqualTo(3000);
            softly.assertThat(discount2).isEqualTo(5000); // 남은 한도
        });
    }

    @Test
    @DisplayName("프로모션 적용된 상품은 할인 대상에서 제외한다.")
    void applyDiscount_ShouldExcludePromotionalItems() {
        Product promotionalProduct = createPromotionalProduct(promotion);
        Product nonPromotionalProduct = createNonPromotionalProduct("NonPromoItem", 10000);
        Orders orders = new Orders(List.of(
                new Order(List.of(promotionalProduct), 1, DateTimes.now()),
                new Order(List.of(nonPromotionalProduct), 1, DateTimes.now()))
        );

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isEqualTo(3000); // 일반 상품에만 할인 적용
    }

    @Test
    @DisplayName("모든 상품이 프로모션 대상일 때 할인 금액은 0원이다.")
    void applyDiscount_ShouldReturnZeroWhenAllItemsArePromotional() {
        Product promotionalProduct = createPromotionalProduct(promotion);
        Orders orders = new Orders(List.of(new Order(List.of(promotionalProduct), 1, DateTimes.now())));

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isZero();
    }

    private Product createNonPromotionalProduct(String name, int price) {
        return new Product(name, price, 1, null);
    }

    private Product createPromotionalProduct(Promotion promotion) {
        return new Product("PromoItem", 10000, 1, promotion);
    }
}
