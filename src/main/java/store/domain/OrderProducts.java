package store.domain;

import java.util.List;
import java.util.Optional;

public class OrderProducts {
    private final List<Product> stocks;

    public OrderProducts(List<Product> stocks) {
        validate(stocks);
        this.stocks = stocks;
    }

    private void validate(List<Product> stocks) {
        if (stocks.isEmpty()) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_INPUT.getMessage());
        }

        validateDifferentProducts(stocks);
    }

    private static void validateDifferentProducts(List<Product> stocks) {
        String productName = stocks.getFirst().getName();
        boolean hasDifferentProducts = stocks.stream().anyMatch(product -> !product.getName().equals(productName));

        if (hasDifferentProducts) {
            throw new IllegalArgumentException(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    public int getMaxCount() {
        return stocks.stream().mapToInt(Product::getQuantity).sum();
    }

    public boolean hasUnclaimedFreeItem(int quantity) {
        return stocks.stream().anyMatch(each -> each.hasUnclaimedFreeItem(quantity));
    }

    public int getNormalProductPrice(int quantity) {
        Optional<Product> normalProduct = normalProduct();
        Integer price = normalProduct.map(Product::getPrice).orElse(0);
        return price * quantity;
    }

    public int getPromotionStock() {
        return stocks.stream().filter(Product::promotionExists).mapToInt(Product::getQuantity).sum();
    }

    public String getProductName() {
        return stocks.getFirst().getName();
    }

    public void decrease(int quantity) {
        int rest = decreasePromotionAmount(quantity);
        decreaseNormalAmount(rest);
    }

    private int decreasePromotionAmount(int quantity) {
        Optional<Product> promotionProduct = stocks.stream().filter(Product::promotionExists).findAny();
        return promotionProduct.map(product -> product.decrease(quantity)).orElse(0);
    }

    private void decreaseNormalAmount(int quantity) {
        normalProduct().ifPresent(product -> product.decrease(quantity));
    }

    private Optional<Product> normalProduct() {
        return stocks.stream().filter(each -> !each.promotionExists()).findFirst();
    }
}
