package store.domain;

public class MemberShip {
    private int eligibleAmount = 8_000;
    private static final double DISCOUNT_RATE = 0.3;

    public int applyDiscount(Orders orders) {
        int normalProductPrice = orders.getNormalProductPrice();
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
