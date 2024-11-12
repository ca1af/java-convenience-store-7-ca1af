package store.domain.order;

import java.util.List;
import java.util.Optional;
import store.domain.DomainErrorMessage;

public class Order {
    private final List<OrderItem> requestedOrderItems;

    public Order(List<OrderItem> requestedOrderItems) {
        validate(requestedOrderItems);
        this.requestedOrderItems = requestedOrderItems;
    }

    private void validate(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        Optional<OrderItem> unavailableOrder = orderItems.stream().filter(each -> !each.hasEnoughStock())
                .findAny();
        if (unavailableOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }

    public List<OrderItem> getRequestedOrders() {
        return List.copyOf(requestedOrderItems);
    }

    public List<PromotionOrderItem> getUnclaimedFreeItemOrder() {
        return requestedOrderItems.stream()
                .filter(PromotionOrderItem.class::isInstance)
                .map(PromotionOrderItem.class::cast)
                .filter(OrderItem::hasUnclaimedFreeItem).toList();
    }

    public List<OrderItem> getPromotedOrders() {
        return requestedOrderItems.stream().filter(PromotionOrderItem.class::isInstance).toList();
    }

    public int getTotalPrice() {
        return requestedOrderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

    public int getPromotionDiscount() {
        return getPromotedOrders().stream().mapToInt(each -> each.calculatePromotedCount() * each.getProductPrice())
                .sum();
    }

    public int getTotalQuantity() {
        return requestedOrderItems.stream().mapToInt(OrderItem::getOrderQuantity).sum();
    }

    public int getNormalProductPrice() {
        return requestedOrderItems.stream().mapToInt(OrderItem::getNormalProductPrice).sum();
    }

    public List<PromotionOrderItem> getFallBackToNormalOrders() {
        return requestedOrderItems.stream().filter(PromotionOrderItem.class::isInstance)
                .map(PromotionOrderItem.class::cast).filter(each -> each.countFallbackToNormal() > 0).toList();
    }

    public void decreaseAmount() {
        requestedOrderItems.forEach(OrderItem::decreaseStocks);
    }
}
