package store.presentation;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.Order;
import store.domain.Product;

public record OrderRequestDto(String productName, int quantity) {
    public Order toDomain(List<Product> stocks, LocalDateTime orderDate) {
        return new Order(stocks, quantity, orderDate);
    }
}
