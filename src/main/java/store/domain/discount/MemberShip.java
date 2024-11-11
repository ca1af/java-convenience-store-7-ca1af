package store.domain.discount;

import store.domain.order.Order;

public class MemberShip {
    private static final double DISCOUNT_RATE = 0.3;
    private int eligibleAmount = 8_000;

    public int applyDiscount(Order order) {
        int normalProductPrice = order.getNormalProductPrice();
        int discountAmount = (int) (normalProductPrice * DISCOUNT_RATE);
        return calculate(discountAmount);
    }

    private int calculate(int discountAmount) {
        if (discountAmount > eligibleAmount) {
            discountAmount = eligibleAmount;
        }

        eligibleAmount = Math.max(eligibleAmount - discountAmount, 0);

        return discountAmount;
    }
}
