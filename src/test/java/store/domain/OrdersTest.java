package store.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.infra.ProductStorage;

class OrdersTest {

    private ProductStorage productStorage;
    private OrderProducts colaProducts;
    private OrderProducts sodaProducts;
    private OrderProducts promotionProducts;

    @BeforeEach
    void setUp() {
        Product cola = new Product("콜라", 1000, 10, null);
        Product soda = new Product("사이다", 1200, 8, null);
        Promotion promotion = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        Product promotionProduct = new Product("감자칩", 1500, 5, promotion);

        productStorage = new ProductStorage(List.of(cola, soda, promotionProduct));

        colaProducts = new OrderProducts(List.of(cola));
        sodaProducts = new OrderProducts(List.of(soda));
        promotionProducts = new OrderProducts(List.of(promotionProduct));
    }

    @DisplayName("무료 증정품이 존재하는 주문만 반환한다")
    @Test
    void getRemaining_ShouldReturnFreeRemainingOrders() {
        // given
        Order order1 = new Order(colaProducts, 5);
        Order order2 = new Order(promotionProducts, 3);

        Orders orders = new Orders(List.of(order1, order2));

        // when
        List<Order> remainingOrders = orders.getFreeRemaining();

        // then
        Assertions.assertThat(remainingOrders).containsExactly(order2);
    }

    @DisplayName("모든 주문이 충족 가능한 경우 validate가 예외를 발생시키지 않는다")
    @Test
    void validate_ShouldNotThrowWhenAllOrdersAreValid() {
        // given
        Order order1 = new Order(colaProducts, 5);
        Order order2 = new Order(sodaProducts, 4);

        Orders orders = new Orders(List.of(order1, order2));

        // when / then
        orders.validate(productStorage);
    }

    @DisplayName("주문 수량이 재고를 초과할 경우 validate가 예외를 발생시킨다")
    @Test
    void validate_ShouldThrowWhenOrderExceedsStock() {
        // given
        Order order1 = new Order(colaProducts, 5);
        Order order2 = new Order(sodaProducts, 10);

        Orders orders = new Orders(List.of(order1, order2));

        // when / then
        assertThatThrownBy(() -> orders.validate(productStorage)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
    }

    @DisplayName("빈 주문 리스트가 들어오면 예외가 발생한다.")
    @Test
    void validate_ShouldThrowWhenOrdersAreEmpty() {
        // given
        List<Order> emptyOrders = List.of();

        // when / then
        assertThatThrownBy(() -> new Orders(emptyOrders)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
    }
}
