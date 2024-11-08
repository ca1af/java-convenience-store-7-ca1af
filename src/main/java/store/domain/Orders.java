package store.domain;

import java.util.List;
import java.util.Optional;
import store.infra.ProductStorage;

public class Orders {
    private final List<Order> requestedOrders;

    public Orders(List<Order> requestedOrders) {
        validateSelf(requestedOrders);
        this.requestedOrders = List.copyOf(requestedOrders);
    }

    public List<Order> getFreeRemaining() {
        return requestedOrders.stream().filter(Order::hasRemain).toList();
    }

    private void validateSelf(List<Order> orders) {
        if (orders.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }
    }

    public void validate(ProductStorage productStorage) {
        Optional<Order> insufficientOrder = requestedOrders.stream()
                .filter(each -> !productStorage.hasQuantity(each.getProductName(), each.getQuantity())).findAny();

        if (insufficientOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }
}
