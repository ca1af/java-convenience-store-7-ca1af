package store.domain.order;

import store.domain.product.NormalProduct;

public class NormalOrderItem extends AbstractOrderItem {
    public NormalOrderItem(NormalProduct normalProduct, int orderQuantity) {
        super(normalProduct, null, orderQuantity);
    }

    @Override
    public boolean hasUnclaimedFreeItem() {
        return false;
    }

    @Override
    public int getPromotionStock() {
        return 0;
    }

    @Override
    public int getNormalProductPrice() {
        return normalProduct.getPrice() * orderQuantity;
    }

    @Override
    public void decreaseStocks() {
        normalProduct.reduceQuantity(orderQuantity);
    }

    @Override
    public int calculatePromotedCount() {
        return 0;
    }
}
