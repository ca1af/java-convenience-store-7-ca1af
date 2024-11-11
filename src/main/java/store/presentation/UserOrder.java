package store.presentation;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.OrderProduct;
import store.domain.Product;

public record UserOrder(String productName, int quantity) {
    public OrderProduct toDomain(List<Product> stocks, LocalDateTime orderDate) {
        return new OrderProduct(stocks, quantity, orderDate);
    }
}
