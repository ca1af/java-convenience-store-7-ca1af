package store.domain.product;

import store.domain.DomainErrorMessage;

public class NormalProduct extends AbstractProduct {
    public NormalProduct(String name, int price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    protected String getPromotionInfo() {
        return "";
    }

    @Override
    public int reduceQuantity(int purchaseQuantity) {
        if (quantity < purchaseQuantity) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
        quantity -= purchaseQuantity;
        return 0;
    }
}
