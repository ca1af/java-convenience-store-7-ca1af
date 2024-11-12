package store.domain.product;

import java.time.LocalDateTime;
import java.util.Objects;
import store.domain.DomainErrorMessage;
import store.domain.discount.Promotion;

public class PromotionProduct extends AbstractProduct {
    private final Promotion promotion;

    public PromotionProduct(String name, int price, int quantity, Promotion promotion) {
        super(name, price, quantity);
        if (Objects.isNull(promotion)) {
            throw new IllegalArgumentException(DomainErrorMessage.PROMOTION_CANNOT_BE_NULL.getMessage());
        }
        this.promotion = promotion;
    }

    @Override
    protected String getPromotionInfo() {
        return " " + promotion.name();
    }

    @Override
    public int reduceQuantity(int purchaseQuantity) {
        if (quantity >= purchaseQuantity) {
            quantity -= purchaseQuantity;
            return 0;
        }

        int remaining = purchaseQuantity - quantity;
        quantity = 0;
        return remaining;
    }

    public int getPromotedCount(int orderQuantity) {
        if (orderQuantity > getQuantity()) {
            return promotion.promotionGetCount(getQuantity());
        }
        return promotion.promotionGetCount(orderQuantity);
    }

    public boolean promotionExists(LocalDateTime orderDate) {
        return promotion.applicable(orderDate);
    }

    public boolean hasUnclaimedFreeItem(int orderQuantity, LocalDateTime orderDate) {
        if (!promotionExists(orderDate) || orderQuantity <= 0) {
            return false;
        }
        if (orderQuantity >= getQuantity()) {
            return false;
        }
        return promotion.hasUnclaimedFreeItem(orderQuantity);
    }
}
