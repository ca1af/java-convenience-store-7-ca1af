package store.domain;

import java.util.List;
import java.util.Optional;

public class Order {
    private final List<OrderProduct> requestedOrderProducts;

    public Order(List<OrderProduct> requestedOrderProducts) {
        validate(requestedOrderProducts);
        this.requestedOrderProducts = requestedOrderProducts;
    }

    private void validate(List<OrderProduct> orderProducts) {
        if (orderProducts.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        Optional<OrderProduct> unavailableOrder = orderProducts.stream().filter(each -> !each.hasEnoughStock()).findAny();
        if (unavailableOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }

    public List<OrderProduct> getRequestedOrders() {
        return List.copyOf(requestedOrderProducts);
    }

    public List<OrderProduct> getUnclaimedFreeItemOrder() {
        return requestedOrderProducts.stream().filter(OrderProduct::hasUnclaimedFreeItem).toList();
    }

    public List<OrderProduct> getPromotedOrders() {
        return requestedOrderProducts.stream().filter(each -> each.calculatePromotedCount() > 0).toList();
    }

    public int getTotalPrice() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::getTotalPrice).sum();
    }

    public int getPromotionDiscount() {
        return getPromotedOrders().stream().mapToInt(order -> order.calculatePromotedCount() * order.getProductPrice())
                .sum();
    }

    public int getTotalQuantity() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::getQuantity).sum();
    }

    public int getNormalProductPrice() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::calculateNormalProductPrice).sum();
    }

    public List<OrderProduct> getFallBackToNormalOrders() {
        return requestedOrderProducts.stream().filter(each -> each.countFallbackToNormal() > 0).toList();
    }

    public void decreaseAmount() {
        requestedOrderProducts.forEach(OrderProduct::decreaseStocks);
    }
}
