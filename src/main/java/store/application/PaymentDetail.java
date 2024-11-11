package store.application;

import store.domain.Order;

public record PaymentDetail(int totalAmount, int promotionDiscount, int totalQuantity, int memberShipDiscount,
                            int paymentAmount) {
    public static PaymentDetail of(Order order, int memberShipDiscount) {
        int totalAmount = order.getTotalPrice();
        int promotionDiscount = order.getPromotionDiscount();
        int paymentAmount = totalAmount - promotionDiscount - memberShipDiscount;
        int totalQuantity = order.getTotalQuantity();
        return new PaymentDetail(totalAmount, promotionDiscount, memberShipDiscount, totalQuantity, paymentAmount);
    }
}
