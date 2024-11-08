package store.domain;

public class Order {
    private final OrderProducts orderProducts;
    private final int quantity;

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

    public boolean hasRemain(){
        return orderProducts.hasRemain(quantity);
    }

    public String getProductName(){
        return orderProducts.getProductName();
    }

    public boolean available() {
        return orderProducts.getMaxCount() >= quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
