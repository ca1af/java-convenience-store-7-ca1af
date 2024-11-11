package store.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTest {
    private List<Product> onePromoOneNormalCola;
    private List<Product> normarSodaStock;
    private List<Product> promotionPotatoChipStock;
    private LocalDateTime orderDate;
    private Product cola;
    private Product colaPromo;

    @BeforeEach
    void setUp() {
        cola = new Product("콜라", 1000, 10, null);
        Product soda = new Product("사이다", 1200, 8, null);
        Promotion promotion = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1));
        colaPromo = new Product("콜라", 1000, 10, promotion);
        Product promotionProduct = new Product("감자칩", 1500, 5, promotion);
        orderDate = DateTimes.now();
        setupProductLists(soda, promotionProduct);
    }

    private void setupProductLists(Product soda, Product promotionProduct) {
        onePromoOneNormalCola = List.of(cola, colaPromo);
        normarSodaStock = List.of(soda);
        promotionPotatoChipStock = List.of(promotionProduct);
    }

    @Test
    @DisplayName("getRequestedOrders 로 불변 리스트를 리턴받는다.")
    void getRequestedOrders() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 4, orderDate);
        Order order = new Order(List.of(orderProduct1));

        List<OrderProduct> requestedOrderProducts = order.getRequestedOrders();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(requestedOrderProducts).containsExactly(orderProduct1);
            softly.assertThatThrownBy(requestedOrderProducts::removeFirst).isInstanceOf(UnsupportedOperationException.class);
        });
    }

    @Test
    @DisplayName("requestedOrders 메서드는 프로모션이 적용된 상품만 반환한다.")
    void getPromotedOrders() {
        OrderProduct orderProduct1 = new OrderProduct(normarSodaStock, 4, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        List<OrderProduct> requestedOrderProducts = order.getPromotedOrders();

        Assertions.assertThat(requestedOrderProducts).containsExactly(orderProduct2);
    }

    @Test
    @DisplayName("getPromotionDiscount 메서드는 프로모션이 적용 금액을 계산한다")
    void getPromotionDiscount() {
        OrderProduct orderProduct1 = new OrderProduct(normarSodaStock, 4, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int promotionDiscount = order.getPromotionDiscount();

        Assertions.assertThat(promotionDiscount).isEqualTo(1500);
    }

    @Test
    @DisplayName("getTotalQuantity 메서드는 총 주문 수량을 계산한다")
    void getTotalQuantity() {
        OrderProduct orderProduct1 = new OrderProduct(normarSodaStock, 4, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int promotionDiscount = order.getTotalQuantity();

        Assertions.assertThat(promotionDiscount).isEqualTo(7);
    }

    @Test
    @DisplayName("상품의 총합 가격을 반환한다.")
    void getTotalPrice() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 4, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);

        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int totalAmount = order.getTotalPrice();

        Assertions.assertThat(totalAmount).isEqualTo(8500);
    }

    @Test
    @DisplayName("무료 증정품이 존재하는 주문만 반환한다")
    void getRemaining_ShouldReturnFreeRemainingOrders() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 4, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);

        Order order = new Order(List.of(orderProduct1, orderProduct2));

        List<OrderProduct> remainingOrderProducts = order.getUnclaimedFreeItemOrder();

        Assertions.assertThat(remainingOrderProducts).containsExactly(orderProduct2);
    }

    @Test
    @DisplayName("모든 주문이 충족 가능한 경우 validate가 예외를 발생시키지 않는다")
    void validate_ShouldNotThrowWhenAllOrdersAreValid() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 5, orderDate);
        OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 4, orderDate);

        Assertions.assertThatCode(() -> new Order(List.of(orderProduct1, orderProduct2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("주문 수량이 재고를 초과할 경우 validate가 예외를 발생시킨다")
    void validate_ShouldThrowWhenOrderExceedsStock() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 15, orderDate); // 재고 초과
        OrderProduct orderProduct2 = new OrderProduct(normarSodaStock, 10, orderDate); // 재고 초과

        List<OrderProduct> orderItemProducts = List.of(orderProduct1, orderProduct2);
        assertThatThrownBy(() -> new Order(orderItemProducts)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("빈 주문 리스트가 들어오면 예외가 발생한다")
    void validate_ShouldThrowWhenOrdersAreEmpty() {
        List<OrderProduct> emptyOrderProducts = List.of();

        assertThatThrownBy(() -> new Order(emptyOrderProducts)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
    }

    @Test
    @DisplayName("일반 상품의 총 가격을 정확히 반환한다")
    void calculateNormalProductPrice_ShouldReturnCorrectSum() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 5, orderDate); // 프로모션이 존재, 따라서 0
        OrderProduct orderProduct2 = new OrderProduct(normarSodaStock, 4, orderDate); // 4 * 1200
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int normalProductPrice = order.getNormalProductPrice();

        Assertions.assertThat(normalProductPrice).isEqualTo(4800);
    }

    @Test
    @DisplayName("일반 재고를 사용해야 하는 주문만 반환한다")
    void getFallBackToNormalOrders_ShouldReturnCorrectOrders() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 15, orderDate); // 프로모션 재고 10, 일반재고 5
        Order order = new Order(List.of(orderProduct1));

        List<OrderProduct> fallbackOrderProducts = order.getFallBackToNormalOrders();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(fallbackOrderProducts).containsExactly(orderProduct1);
            softly.assertThat(fallbackOrderProducts.getFirst().countFallbackToNormal()).isEqualTo(5);
        });
    }

    @Test
    @DisplayName("getFallBackToNormalOrders: 프로모션 재고로만 충족 가능한 주문은 반환하지 않는다")
    void getFallBackToNormalOrders_ShouldExcludeFullyPromotionalOrders() {
        OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 8, orderDate); // 프로모션 재고로 충족 가능
        Order order = new Order(List.of(orderProduct1));

        List<OrderProduct> fallbackOrderProducts = order.getFallBackToNormalOrders();

        Assertions.assertThat(fallbackOrderProducts).isEmpty();
    }

    @Nested
    @DisplayName("decreaseAmount 테스트")
    class DecreaseAmount {
        @Test
        @DisplayName("decreaseAmount 메서드는 총 주문 수량을 계산한다")
        void decreaseAmount() {
            OrderProduct orderProduct1 = new OrderProduct(normarSodaStock, 4, orderDate);
            OrderProduct orderProduct2 = new OrderProduct(promotionPotatoChipStock, 3, orderDate);
            Order order = new Order(List.of(orderProduct1, orderProduct2));

            int promotionDiscount = order.getTotalQuantity();

            Assertions.assertThat(promotionDiscount).isEqualTo(7);
        }

        @Test
        @DisplayName("decreaseAmount 메서드는 프로모션 재고 먼저 차감하고 그 후 일반 재고를 차감한다.")
        void decreaseAmount_promo() {
            OrderProduct orderProduct1 = new OrderProduct(onePromoOneNormalCola, 14, orderDate);
            Order order = new Order(List.of(orderProduct1));

            order.decreaseAmount();

            SoftAssertions.assertSoftly(softly -> {
                Assertions.assertThat(colaPromo.getQuantity()).isZero();
                Assertions.assertThat(cola.getQuantity()).isEqualTo(6);
            });
        }
    }
}
