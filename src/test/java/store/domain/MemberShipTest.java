package store.domain;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("MemberShip 할인 로직 테스트")
class MemberShipTest {

    private MemberShip memberShip;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        memberShip = new MemberShip();
        promotion = new Promotion("Sample Promotion", 2, 1, LocalDate.now(), LocalDate.now().plusDays(10));
    }

    @DisplayName("할인을 적용한다.")
    @ParameterizedTest
    @CsvSource({
            "10000, 3000",
            "5000, 1500",
            "8000, 2400"
    })
    void apply30PercentDiscount(int price, int expectedDiscount) {
        Product product = new Product("NonPromoItem", price, 1, null);
        Orders orders = new Orders(List.of(new Order(new OrderProducts(List.of(product)), 1)));

        int discount = memberShip.applyDiscount(orders);

        Assertions.assertThat(discount).isEqualTo(expectedDiscount);
    }

    @DisplayName("할인 금액이 남은 한도를 초과하면, 최대 한도까지만 할인을 적용한다.")
    @Test
    void applyDiscount_shouldLimitDiscountToEligibleAmount() {
        Product expensiveProduct = new Product("ExpensiveItem", 26667, 1, null); // 경계값
        Orders orders = new Orders(List.of(new Order(new OrderProducts(List.of(expensiveProduct)), 1)));

        int discount = memberShip.applyDiscount(orders);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(discount).isEqualTo(8000); // 최대 할인 한도
        });
    }

    @DisplayName("남은 할인 한도를 초과하지 않도록 할인을 적용한다.")
    @Test
    void applyDiscount_shouldNotExceedEligibleAmountAfterMultipleDiscounts() {
        Product product1 = new Product("Item1", 10000, 1, null);
        Product expensiveProduct = new Product("ExpensiveItem", 26667, 1, null); // 경계값
        Orders orders1 = new Orders(List.of(new Order(new OrderProducts(List.of(product1)), 1)));
        Orders orders2 = new Orders(List.of(new Order(new OrderProducts(List.of(expensiveProduct)), 1)));

        int discount1 = memberShip.applyDiscount(orders1);
        int discount2 = memberShip.applyDiscount(orders2);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(discount1).isEqualTo(3000);
            softly.assertThat(discount2).isEqualTo(5000); // 남은 한도
        });
    }

    @DisplayName("프로모션이 적용된 상품은 할인 대상에서 제외한다.")
    @Test
    void applyDiscount_shouldExcludePromotionalItems() {
        Product promotionalProduct = new Product("PromoItem", 10000, 1, promotion); // 프로모션 적용 상품
        Product nonPromotionalProduct = new Product("NonPromoItem", 10000, 1, null); // 프로모션 없음
        Orders orders = new Orders(List.of(
                new Order(new OrderProducts(List.of(promotionalProduct)), 1),
                new Order(new OrderProducts(List.of(nonPromotionalProduct)), 1)
        ));

        int discount = memberShip.applyDiscount(orders);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(discount).isEqualTo(3000); // 프로모션 없는 상품만 할인
        });
    }
}
