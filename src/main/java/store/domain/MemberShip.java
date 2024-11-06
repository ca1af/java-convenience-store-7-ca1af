package store.domain;

import java.util.List;

public class MemberShip {
    private int eligibleAmount = 8_000;
    private static final double DISCOUNT_RATE = 0.3;

    public int applyDiscount(List<Product> products) {
        int totalPrice = products.stream()
                .filter(product -> !product.promotionExists())
                .mapToInt(Product::getTotalPrice)
                .sum();

        int discountAmount = (int) (totalPrice * DISCOUNT_RATE);
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
