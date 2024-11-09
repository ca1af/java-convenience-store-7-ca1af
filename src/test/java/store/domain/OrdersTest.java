package store.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdersTest {

    private List<Product> colaStocks;
    private List<Product> sodaStocks;
    private List<Product> promotionStocks;

    @BeforeEach
    void setUp() {
        Product cola = new Product("콜라", 1000, 10, null);
        Product soda = new Product("사이다", 1200, 8, null);
        Promotion promotion = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        Product colaPromo = new Product("콜라", 1000, 10, promotion);
        Product promotionProduct = new Product("감자칩", 1500, 5, promotion);

        colaStocks = List.of(cola, colaPromo);
        sodaStocks = List.of(soda);
        promotionStocks = List.of(promotionProduct);
    }

    @DisplayName("무료 증정품이 존재하는 주문만 반환한다")
    @Test
    void getRemaining_ShouldReturnFreeRemainingOrders() {
        // given
        Order order1 = new Order(colaStocks, 4);
        Order order2 = new Order(promotionStocks, 3);

        Orders orders = new Orders(List.of(order1, order2));

        // when
        List<Order> remainingOrders = orders.getUnclaimedFreeItemOrder();

        // then
        Assertions.assertThat(remainingOrders).containsExactly(order2);
    }

    @DisplayName("모든 주문이 충족 가능한 경우 validate가 예외를 발생시키지 않는다")
    @Test
    void validate_ShouldNotThrowWhenAllOrdersAreValid() {
        // given
        Order order1 = new Order(colaStocks, 5);
        Order order2 = new Order(sodaStocks, 4);

        // when / then
        Assertions.assertThatCode(() -> new Orders(List.of(order1, order2))).doesNotThrowAnyException();
    }

    @DisplayName("주문 수량이 재고를 초과할 경우 validate가 예외를 발생시킨다")
    @Test
    void validate_ShouldThrowWhenOrderExceedsStock() {
        // given
        Order order1 = new Order(colaStocks, 15); // 재고 초과
        Order order2 = new Order(sodaStocks, 10); // 재고 초과

        List<Order> orderItems = List.of(order1, order2);
        // when / then
        assertThatThrownBy(() -> new Orders(orderItems))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
    }

    @DisplayName("빈 주문 리스트가 들어오면 예외가 발생한다")
    @Test
    void validate_ShouldThrowWhenOrdersAreEmpty() {
        // given
        List<Order> emptyOrders = List.of();

        // when / then
        assertThatThrownBy(() -> new Orders(emptyOrders))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
    }

    @DisplayName("일반 상품의 총 가격을 정확히 반환한다")
    @Test
    void getNormalProductPrice_ShouldReturnCorrectSum() {
        // given
        Order order1 = new Order(colaStocks, 5); // 프로모션이 존재, 따라서 0
        Order order2 = new Order(sodaStocks, 4); // 4 * 1200

        Orders orders = new Orders(List.of(order1, order2));

        // when
        int normalProductPrice = orders.getNormalProductPrice();

        // then
        Assertions.assertThat(normalProductPrice).isEqualTo(4800);
    }

    @DisplayName("일반 재고를 사용해야 하는 주문만 반환한다")
    @Test
    void getFallBackToNormalOrders_ShouldReturnCorrectOrders() {
        // given
        Order order1 = new Order(colaStocks, 15); // 프로모션 재고 10, 일반재고 5
        Orders orders = new Orders(List.of(order1));

        // when
        List<Order> fallbackOrders = orders.getFallBackToNormalOrders();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(fallbackOrders).containsExactly(order1);
            softly.assertThat(fallbackOrders.getFirst().countFallbackToNormal()).isEqualTo(5);
        });
    }

    @DisplayName("getFallBackToNormalOrders: 프로모션 재고로만 충족 가능한 주문은 반환하지 않는다")
    @Test
    void getFallBackToNormalOrders_ShouldExcludeFullyPromotionalOrders() {
        // given
        Order order1 = new Order(colaStocks, 8); // 프로모션 재고로 충족 가능

        Orders orders = new Orders(List.of(order1));

        // when
        List<Order> fallbackOrders = orders.getFallBackToNormalOrders();

        // then
        Assertions.assertThat(fallbackOrders).isEmpty();
    }
}
