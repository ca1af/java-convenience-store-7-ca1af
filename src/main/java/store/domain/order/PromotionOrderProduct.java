package store.domain.order;

import java.time.LocalDateTime;
import store.domain.DomainErrorMessage;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

public class PromotionOrderProduct extends AbstractOrderProduct {
    private final LocalDateTime orderDate;

    public PromotionOrderProduct(NormalProduct normalProduct, PromotionProduct promotionProduct, int orderQuantity,
                                 LocalDateTime orderDate) {
        super(normalProduct, promotionProduct, orderQuantity);
        this.orderDate = orderDate;
        validateDifferentProducts(normalProduct, promotionProduct);
    }

    private static void validateDifferentProducts(NormalProduct normalProduct, PromotionProduct promotionProduct) {
        if (!normalProduct.getName().equals(promotionProduct.getName())) {
            throw new IllegalArgumentException(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    @Override
    public boolean hasUnclaimedFreeItem() {
        if (getPromotionStock() < orderQuantity + 1) {
            return false;
        }
        return promotionProduct.hasUnclaimedFreeItem(orderQuantity, orderDate);
    }

    @Override
    public int getPromotionStock() {
        if (!promotionProduct.promotionExists(orderDate)){
            return 0;
        }
        return promotionProduct.getQuantity();
    }

    @Override
    public int getNormalProductPrice() {
        int promotionUnitsUsed = Math.min(orderQuantity, getPromotionStock());
        int normalUnitsUsed = orderQuantity - promotionUnitsUsed;
        return normalProduct.getPrice() * normalUnitsUsed;
    }

    @Override
    public boolean hasEnoughStock() {
        int promotionStock = getPromotionStock();
        int normalStock = normalProduct.getQuantity();
        return (promotionStock + normalStock) >= orderQuantity;
    }

    @Override
    public void decreaseStocks() {
        int remainingQuantity = promotionProduct.reduceQuantity(orderQuantity);
        normalProduct.reduceQuantity(remainingQuantity);
    }

    @Override
    public int calculatePromotedCount() {
        if (!promotionProduct.promotionExists(orderDate)){
            return 0;
        }
        return promotionProduct.getPromotedCount(orderQuantity);
    }

    public int countFallbackToNormal() {
        int promotionStock = promotionProduct.getQuantity();
        return Math.max(orderQuantity - promotionStock, 0);
    }
}
