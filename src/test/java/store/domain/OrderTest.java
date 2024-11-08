package store.domain;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderTest {
    private final Promotion onePlusOne = new Promotion("1+1", 1, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
    private final Product product = new Product("foo", 1000, 10, onePlusOne);
    private final OrderProducts productStore = new OrderProducts(List.of(product));

    @Test
    void available_true() {
        Order order = new Order(productStore, 10);
        Assertions.assertThat(order.available()).isTrue();
    }

    @Test
    void available_false() {
        Order order = new Order(productStore, 20);
        Assertions.assertThat(order.available()).isFalse();
    }

    @Test
    void promotionAvailable_true() {
        Order order = new Order(productStore, 9);
        Assertions.assertThat(order.hasRemain()).isTrue();
    }
}
