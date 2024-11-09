package store.domain;

import java.util.List;

public record Receipt(String name, int quantity, int totalPrice) {
    public static Receipt of(Order order) {
        return new Receipt(order.getProductName(), order.getQuantity(), order.getTotalPrice());
    }

    public static Receipt ofPromotion(Order order) {
        return new Receipt(order.getProductName(), order.getPromotedCount(), 0);
    }

    public static List<Receipt> ofList(Orders orders) {
        return orders.getRequestedOrders().stream()
                .map(Receipt::of)
                .toList();
    }

    public static List<Receipt> ofPromotedOrders(Orders orders) {
        return orders.getPromotedOrders().stream()
                .filter(order -> order.getPromotedCount() > 0)
                .map(Receipt::ofPromotion)
                .toList();
    }

    // TODO : 포맷을 좀 더 간결히 할 수 없는지 고민한다. "상품영수증, 증정영수증, 결제영수증"
    public String format() {
        if (totalPrice == 0) {
            return String.format("%-10s\t %2d", name, quantity);
        }
        return String.format("%-10s\t %2d\t%,6d", name, quantity, totalPrice);
    }
}
