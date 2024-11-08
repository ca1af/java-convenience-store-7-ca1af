package store.domain;

import java.util.List;
import java.util.Optional;

public class Orders {
    private final List<Order> requestedOrders;

    public Orders(List<Order> requestedOrders) {
        validateSelf(requestedOrders);
        this.requestedOrders = requestedOrders;
    }

    public List<Order> getUnclaimedFreeItemOrder() {
        return requestedOrders.stream().filter(Order::hasUnclaimedFreeItem).toList();
    }

    public boolean hasUnclaimedFreeItem() {
        return requestedOrders.stream().anyMatch(Order::hasUnclaimedFreeItem);
    }

    private void validateSelf(List<Order> orders) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        Optional<Order> unavailableOrder = orders.stream().filter(each -> !each.available()).findAny();
        if (unavailableOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }

    public int getNormalProductPrice(){
        return requestedOrders.stream().mapToInt(Order::getNormalProductPrice).sum();
    }

    public List<Order> getFallBackToNormalOrders(){
        return requestedOrders.stream()
                .filter(each -> each.countFallbackToNormal() > 0)
                .toList();
    }

    public void decreaseAmount(){
        requestedOrders.forEach(Order::decreaseAmount);
    }
}
