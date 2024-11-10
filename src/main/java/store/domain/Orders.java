package store.domain;

import java.util.List;
import java.util.Optional;

public class Orders {
    private final List<Order> requestedOrders;

    public Orders(List<Order> requestedOrders) {
        validate(requestedOrders);
        this.requestedOrders = requestedOrders;
    }

    private void validate(List<Order> orders) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        Optional<Order> unavailableOrder = orders.stream().filter(each -> !each.available()).findAny();
        if (unavailableOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }

    public List<Order> getRequestedOrders() {
        return List.copyOf(requestedOrders);
    }

    public List<Order> getUnclaimedFreeItemOrder() {
        return requestedOrders.stream().filter(Order::hasUnclaimedFreeItem).toList();
    }

    public List<Order> getPromotedOrders() {
        return requestedOrders.stream().filter(each -> each.getPromotedCount() > 0).toList();
    }

    public int getTotalPrice() {
        return requestedOrders.stream().mapToInt(Order::getTotalPrice).sum();
    }

    public int getPromotionDiscount() {
        return getPromotedOrders().stream().mapToInt(order -> order.getPromotedCount() * order.getProductPrice()).sum();
    }

    public int getTotalQuantity() {
        return requestedOrders.stream().mapToInt(Order::getQuantity).sum();
    }

    public int getNormalProductPrice() {
        return requestedOrders.stream().mapToInt(Order::getNormalProductPrice).sum();
    }

    public List<Order> getFallBackToNormalOrders() {
        return requestedOrders.stream().filter(each -> each.countFallbackToNormal() > 0).toList();
    }

    public void decreaseAmount() {
        requestedOrders.forEach(Order::decreaseStocks);
    }
}
