package store.domain;

public class Order {
    private final OrderProducts orderProducts;
    private int quantity;

    public Order(OrderProducts orderProducts, int quantity) {
        validate(quantity);
        this.orderProducts = orderProducts;
        this.quantity = quantity;
    }

    private void validate(int quantity) {
        if (quantity <= 0){
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }

    public void addQuantity(){
        this.quantity++;
    }

    public void decreaseQuantity(int decreaseAmount){
        this.quantity -= decreaseAmount;
    }

    // 프로모션 상품이 있지만 일반 상품을 써야하는 경우, 써야 하는 일반 상품의 갯수를 계산
    public int countFallbackToNormal() {
        int promotionStock = orderProducts.getPromotionStock();
        if (promotionStock >= quantity) {
            return 0;
        }
        return quantity - promotionStock;
    }

    public int getNormalProductPrice(){
        return orderProducts.getNormalProductPrice(getNormalProductQuantity());
    }

    private int getNormalProductQuantity() {
        int promotionStockUsed = Math.min(quantity, orderProducts.getPromotionStock());
        return quantity - promotionStockUsed;
    }

    public boolean hasUnclaimedFreeItem(){
        return orderProducts.hasUnclaimedFreeItem(quantity);
    }

    public boolean hasFallbackToNormal(){
        int promotionStock = orderProducts.getPromotionStock();
        return quantity > promotionStock;
    }

    public boolean available() {
        return orderProducts.getMaxCount() >= quantity;
    }

    public String getProductName(){
        return this.orderProducts.getProductName();
    }

    public void decreaseAmount(){
        orderProducts.decrease(quantity);
    }
}
