package store.domain;

public class Order {
    private final Product product;
    private final int quantity;

    // 주문 수량이 온전히 제공되었는가?
    public Order(Product product, int quantity) {
        validate(quantity);
        this.product = product;
        this.quantity = quantity;
    }

    private void validate(int quantity) {
        if (quantity <= 0){
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }

    public boolean hasRemain(){
        if (!product.promotionExists()){
            return false;
        }

        return product.hasRemains(quantity);
    }

    public boolean available() {
        return product.getQuantity() >= quantity;
    }

    public String getProductName(){
        return product.getName();
    }

    public int getQuantity() {
        return quantity;
    }
}
