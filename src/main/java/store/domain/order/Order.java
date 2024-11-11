package store.domain.order;

import java.util.List;
import java.util.Optional;
import store.domain.DomainErrorMessage;

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

        Optional<OrderProduct> unavailableOrder = orderProducts.stream().filter(each -> !each.hasEnoughStock())
                .findAny();
        if (unavailableOrder.isPresent()) {
            throw new IllegalArgumentException(DomainErrorMessage.QUANTITY_EXCEEDED.getMessage());
        }
    }

    public List<OrderProduct> getRequestedOrders() {
        return List.copyOf(requestedOrderProducts);
    }

    public List<PromotionOrderProduct> getUnclaimedFreeItemOrder() {
        return requestedOrderProducts.stream()
                .filter(PromotionOrderProduct.class::isInstance)
                .map(PromotionOrderProduct.class::cast)
                .filter(OrderProduct::hasUnclaimedFreeItem).toList();
    }

    public List<OrderProduct> getPromotedOrders() {
        return requestedOrderProducts.stream().filter(PromotionOrderProduct.class::isInstance).toList();
    }

    public int getTotalPrice() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::getTotalPrice).sum();
    }

    public int getPromotionDiscount() {
        return getPromotedOrders().stream().mapToInt(each -> each.calculatePromotedCount() * each.getProductPrice())
                .sum();
    }

    public int getTotalQuantity() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::getOrderQuantity).sum();
    }

    public int getNormalProductPrice() {
        return requestedOrderProducts.stream().mapToInt(OrderProduct::getNormalProductPrice).sum();
    }

    public List<PromotionOrderProduct> getFallBackToNormalOrders() {
        return requestedOrderProducts.stream().filter(PromotionOrderProduct.class::isInstance)
                .map(PromotionOrderProduct.class::cast).filter(each -> each.countFallbackToNormal() > 0).toList();
    }

    public void decreaseAmount() {
        requestedOrderProducts.forEach(OrderProduct::decreaseStocks);
    }
}
