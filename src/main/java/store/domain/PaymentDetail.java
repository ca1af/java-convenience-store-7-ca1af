package store.domain;

public record PaymentDetail(int totalAmount, int promotionDiscount, int totalQuantity, int memberShipDiscount, int paymentAmount) {
    public static PaymentDetail of(Orders orders, int memberShipDiscount) {
        int totalAmount = orders.getTotalPrice();
        int promotionDiscount = orders.getPromotionDiscount();
        int paymentAmount = totalAmount - promotionDiscount - memberShipDiscount;
        int totalQuantity = orders.getTotalQuantity();
        return new PaymentDetail(totalAmount, promotionDiscount, memberShipDiscount, totalQuantity, paymentAmount);
    }
}
