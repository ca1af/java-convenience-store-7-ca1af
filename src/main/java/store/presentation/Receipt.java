package store.presentation;

import java.util.List;
import store.domain.Order;
import store.domain.Orders;

public record Receipt(String name, int quantity, int totalPrice) {
    public static Receipt of(Order order) {
        return new Receipt(order.getProductName(), order.getQuantity(), order.getTotalPrice());
    }

    public static Receipt ofPromotion(Order order) {
        return new Receipt(order.getProductName(), order.calculatePromotedCount(), 0);
    }

    public static List<Receipt> ofList(Orders orders) {
        return orders.getRequestedOrders().stream()
                .map(Receipt::of)
                .toList();
    }

    public static List<Receipt> ofPromotedOrders(Orders orders) {
        return orders.getPromotedOrders().stream()
                .filter(order -> order.calculatePromotedCount() > 0)
                .map(Receipt::ofPromotion)
                .toList();
    }
    
    public String format() {
        if (totalPrice == 0) {
            return String.format("%-10s\t %2d", name, quantity);
        }
        return String.format("%-10s\t %2d\t%,6d", name, quantity, totalPrice);
    }
}
