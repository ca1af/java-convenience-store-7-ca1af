package store.domain.order;

import store.domain.DomainErrorMessage;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

public abstract class AbstractOrderItem implements OrderItem {
    protected final NormalProduct normalProduct;
    protected final PromotionProduct promotionProduct;
    protected int orderQuantity;

    protected AbstractOrderItem(NormalProduct normalProduct, PromotionProduct promotionProduct, int orderQuantity) {
        validate(normalProduct);
        this.normalProduct = normalProduct;
        this.promotionProduct = promotionProduct;
        this.orderQuantity = orderQuantity;
    }

    private void validate(NormalProduct normalProduct){
        if (normalProduct == null) {
            throw new IllegalArgumentException(DomainErrorMessage.NORMAL_PRODUCT_NEEDED.getMessage());
        }
    }

    @Override
    public void addQuantity() {
        orderQuantity++;
    }

    @Override
    public void subtract(int amount) {
        orderQuantity -= amount;
    }

    @Override
    public boolean hasEnoughStock() {
        return normalProduct.getQuantity() >= orderQuantity;
    }

    @Override
    public int getTotalPrice() {
        return normalProduct.getPrice() * orderQuantity;
    }

    public int getProductPrice(){
        return normalProduct.getPrice();
    }

    @Override
    public int getOrderQuantity() {
        return orderQuantity;
    }

    @Override
    public String getProductName() {
        return normalProduct.getName();
    }

    @Override
    public boolean hasFallbackToNormal() {
        if (promotionProduct == null) {
            return false;
        }
        return orderQuantity> getPromotionStock();
    }
}

