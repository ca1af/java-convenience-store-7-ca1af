package store.domain.product;

public interface Product {
    int reduceQuantity(int purchaseQuantity);
    String getName();
    int getPrice();
    int getQuantity();
    String toString();
}
