package store.application;

import java.util.List;
import store.domain.order.Order;
import store.domain.order.OrderItem;

public record Receipt(String name, int quantity, int totalPrice) {
    public static Receipt of(OrderItem orderItem) {
        return new Receipt(orderItem.getProductName(), orderItem.getOrderQuantity(), orderItem.getTotalPrice());
    }

    public static Receipt ofPromotion(OrderItem orderItem) {
        return new Receipt(orderItem.getProductName(), orderItem.calculatePromotedCount(), 0);
    }

    public static List<Receipt> ofList(Order order) {
        return order.getRequestedOrders().stream().map(Receipt::of).toList();
    }

    public static List<Receipt> ofPromotedOrders(Order orders) {
        return orders.getPromotedOrders().stream().filter(order -> order.calculatePromotedCount() > 0)
                .map(Receipt::ofPromotion).toList();
    }

    public String format() {
        if (totalPrice == 0) {
            return String.format("%-10s\t %2d", name, quantity);
        }
        return String.format("%-10s\t %2d\t%,6d", name, quantity, totalPrice);
    }
}
