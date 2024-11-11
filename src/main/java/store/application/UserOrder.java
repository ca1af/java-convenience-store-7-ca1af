package store.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import store.domain.DomainErrorMessage;
import store.domain.order.NormalOrderItem;
import store.domain.order.OrderItem;
import store.domain.order.PromotionOrderItem;
import store.domain.product.NormalProduct;
import store.domain.product.Product;
import store.domain.product.PromotionProduct;

public record UserOrder(String productName, int quantity) {
    public OrderItem toDomain(List<Product> products, LocalDateTime orderDate) {
        Optional<NormalProduct> normalProduct = getNormalProduct(products);
        Optional<PromotionProduct> promotionProduct = getPromotionProduct(products);
        if (normalProduct.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
        }
        if (promotionProduct.isPresent()) {
            return new PromotionOrderItem(normalProduct.get(), promotionProduct.get(), quantity, orderDate);
        }
        return new NormalOrderItem(normalProduct.get(), quantity);
    }

    private Optional<PromotionProduct> getPromotionProduct(List<Product> products) {
        return products.stream().filter(PromotionProduct.class::isInstance).map(PromotionProduct.class::cast)
                .findFirst();
    }

    private Optional<NormalProduct> getNormalProduct(List<Product> products) {
        return products.stream().filter(NormalProduct.class::isInstance).map(NormalProduct.class::cast).findFirst();
    }
}
