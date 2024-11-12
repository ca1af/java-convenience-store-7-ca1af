package store.domain.order;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import store.domain.DomainErrorMessage;
import store.domain.discount.Promotion;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

class OrderTest {
    private NormalProduct cola;
    private NormalProduct soda;
    private PromotionProduct colaOnePlusOne;
    private PromotionProduct sodaOnePlusOne;
    private LocalDateTime orderDate;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        cola = new NormalProduct("콜라", 1000, 10);
        soda = new NormalProduct("사이다", 1200, 8);
        promotion = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        colaOnePlusOne = new PromotionProduct("콜라", 1000, 10, promotion);
        sodaOnePlusOne = new PromotionProduct("사이다", 1200, 5, promotion);
        orderDate = LocalDateTime.now();
    }

    @Test
    @DisplayName("getRequestedOrders 로 불변 리스트를 리턴받는다.")
    void getRequestedOrders() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 4, orderDate);
        Order order = new Order(List.of(orderProduct1));

        List<OrderItem> requestedOrderItems = order.getRequestedOrders();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(requestedOrderItems).containsExactly(orderProduct1);
            softly.assertThatThrownBy(() -> requestedOrderItems.remove(0))
                    .isInstanceOf(UnsupportedOperationException.class);
        });
    }

    @Test
    @DisplayName("getPromotedOrders 메서드는 프로모션이 적용된 상품만 반환한다.")
    void getPromotedOrders() {
        NormalOrderItem orderProduct1 = new NormalOrderItem(soda, 4);
        PromotionOrderItem orderProduct2 = new PromotionOrderItem(cola, colaOnePlusOne, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        List<OrderItem> promotedOrderItems = order.getPromotedOrders();

        Assertions.assertThat(promotedOrderItems).containsExactly(orderProduct2);
    }

    @Test
    @DisplayName("getPromotionDiscount 메서드는 프로모션이 적용 금액을 계산한다")
    void getPromotionDiscount() {
        NormalOrderItem orderProduct1 = new NormalOrderItem(soda, 4);
        PromotionOrderItem orderProduct2 = new PromotionOrderItem(cola, colaOnePlusOne, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int promotionDiscount = order.getPromotionDiscount();

        Assertions.assertThat(promotionDiscount).isEqualTo(1000 * orderProduct2.calculatePromotedCount());
    }

    @Test
    @DisplayName("getTotalQuantity 메서드는 총 주문 수량을 계산한다")
    void getTotalQuantity() {
        NormalOrderItem orderProduct1 = new NormalOrderItem(soda, 4);
        PromotionOrderItem orderProduct2 = new PromotionOrderItem(cola, colaOnePlusOne, 3, orderDate);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int totalQuantity = order.getTotalQuantity();

        Assertions.assertThat(totalQuantity).isEqualTo(7);
    }

    @Test
    @DisplayName("상품의 총합 가격을 반환한다.")
    void getTotalPrice() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 4, orderDate);
        NormalOrderItem orderProduct2 = new NormalOrderItem(cola, 3);

        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int totalAmount = order.getTotalPrice();

        Assertions.assertThat(totalAmount).isEqualTo(orderProduct1.getTotalPrice() + orderProduct2.getTotalPrice());
    }

    @Test
    @DisplayName("무료 증정품이 존재하는 주문만 반환한다")
    void getRemaining_ShouldReturnFreeRemainingOrders() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 1, orderDate);
        PromotionOrderItem orderProduct2 = new PromotionOrderItem(soda, sodaOnePlusOne, 3, orderDate);

        Order order = new Order(List.of(orderProduct1, orderProduct2));

        List<PromotionOrderItem> remainingOrderProducts = order.getUnclaimedFreeItemOrder();

        Assertions.assertThat(remainingOrderProducts).containsExactlyInAnyOrder(orderProduct1, orderProduct2);
    }

    @Test
    @DisplayName("모든 주문이 충족 가능한 경우 validate가 예외를 발생시키지 않는다")
    void validate_ShouldNotThrowWhenAllOrdersAreValid() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 5, orderDate);
        PromotionOrderItem orderProduct2 = new PromotionOrderItem(soda, sodaOnePlusOne, 4, orderDate);

        Assertions.assertThatCode(() -> new Order(List.of(orderProduct1, orderProduct2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("주문 수량이 재고를 초과할 경우 validate가 예외를 발생시킨다")
    void validate_ShouldThrowWhenOrderExceedsStock() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 25, orderDate); // 재고 초과
        NormalOrderItem orderProduct2 = new NormalOrderItem(soda, 10); // 재고 초과

        List<OrderItem> orderItemProducts = List.of(orderProduct1, orderProduct2);
        assertThatThrownBy(() -> new Order(orderItemProducts)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("빈 주문 리스트가 들어오면 예외가 발생한다")
    void validate_ShouldThrowWhenOrdersAreEmpty() {
        List<OrderItem> emptyOrderItems = List.of();

        assertThatThrownBy(() -> new Order(emptyOrderItems)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.INVALID_INPUT.getMessage());
    }

    @Test
    @DisplayName("일반 상품의 총 가격을 정확히 반환한다")
    void calculateNormalProductPrice_ShouldReturnCorrectSum() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 5, orderDate);
        NormalOrderItem orderProduct2 = new NormalOrderItem(soda, 4);
        Order order = new Order(List.of(orderProduct1, orderProduct2));

        int normalProductPrice = order.getNormalProductPrice();

        Assertions.assertThat(normalProductPrice).isEqualTo(orderProduct2.getTotalPrice());
    }

    @Test
    @DisplayName("일반 재고를 사용해야 하는 주문만 반환한다")
    void getFallBackToNormalOrders_ShouldReturnCorrectOrders() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 15, orderDate); // 프로모션 재고 10, 일반재고 10

        Order order = new Order(List.of(orderProduct1));

        List<PromotionOrderItem> fallbackOrderProducts = order.getFallBackToNormalOrders();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(fallbackOrderProducts).containsExactly(orderProduct1);
            softly.assertThat(fallbackOrderProducts.getFirst().countFallbackToNormal()).isEqualTo(5);
        });
    }

    @Test
    @DisplayName("getFallBackToNormalOrders: 프로모션 재고로만 충족 가능한 주문은 반환하지 않는다")
    void getFallBackToNormalOrders_ShouldExcludeFullyPromotionalOrders() {
        PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 8, orderDate); // 프로모션 재고로 충족 가능
        Order order = new Order(List.of(orderProduct1));

        List<PromotionOrderItem> fallbackOrderProducts = order.getFallBackToNormalOrders();

        Assertions.assertThat(fallbackOrderProducts).isEmpty();
    }

    @Nested
    @DisplayName("decreaseAmount 테스트")
    class DecreaseAmount {
        @Test
        @DisplayName("decreaseAmount 메서드는 각 상품의 재고를 정확히 감소시킨다")
        void decreaseAmount() {
            NormalOrderItem orderProduct1 = new NormalOrderItem(soda, 4);
            PromotionOrderItem orderProduct2 = new PromotionOrderItem(cola, colaOnePlusOne, 3, orderDate);
            Order order = new Order(List.of(orderProduct1, orderProduct2));

            order.decreaseAmount();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(soda.getQuantity()).isEqualTo(4); // 8 - 4
                softly.assertThat(colaOnePlusOne.getQuantity()).isEqualTo(7); // 10 - 3
                softly.assertThat(cola.getQuantity()).isEqualTo(10); // 변경 없음
            });
        }

        @Test
        @DisplayName("decreaseAmount 메서드는 프로모션 재고 먼저 차감하고 그 후 일반 재고를 차감한다.")
        void decreaseAmount_promo() {
            PromotionOrderItem orderProduct1 = new PromotionOrderItem(cola, colaOnePlusOne, 14, orderDate);
            Order order = new Order(List.of(orderProduct1));

            order.decreaseAmount();

            SoftAssertions.assertSoftly(softly -> {
                Assertions.assertThat(colaOnePlusOne.getQuantity()).isZero(); // 10 - 10
                Assertions.assertThat(cola.getQuantity()).isEqualTo(6); // 10 - 4
            });
        }
    }
}
