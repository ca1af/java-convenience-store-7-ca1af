package store.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Order {
    private final List<Product> products;
    private final LocalDateTime orderDate;
    private int quantity;

    public Order(List<Product> products, int quantity, LocalDateTime orderDate) {
        this.orderDate = orderDate;
        validateDifferentProducts(products);
        this.products = products;
        this.quantity = quantity;
    }

    private static void validateDifferentProducts(List<Product> stocks) {
        String productName = stocks.getFirst().getName();
        boolean hasDifferentProducts = stocks.stream().anyMatch(product -> !product.getName().equals(productName));
        if (hasDifferentProducts) {
            throw new IllegalArgumentException(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    public void addQuantity() {
        this.quantity++;
    }

    public void subtract(int amount) {
        this.quantity -= amount;
    }

    public int countFallbackToNormal() {
        int promotionStock = getPromotionStock();
        return Math.max(quantity - promotionStock, 0);
    }

    public int calculateNormalProductPrice() {
        Optional<Product> normalProduct = getNormalProduct();
        return normalProduct.map(product -> product.getPrice() * getNormalProductQuantity()).orElse(0);
    }

    public int calculatePromotedCount() {
        Optional<Product> promotionProduct = getPromotionProduct();
        return promotionProduct.map(product -> product.getPromotedCount(quantity)).orElse(0);
    }

    public boolean hasUnclaimedFreeItem() {
        return products.stream().anyMatch(product -> product.hasUnclaimedFreeItem(quantity, orderDate));
    }

    public boolean hasFallbackToNormal() {
        if (getPromotionProduct().isEmpty()) {
            return false;
        }
        return quantity > getPromotionStock();
    }

    public boolean hasEnoughStock() {
        return getTotalAvailableQuantity() >= quantity;
    }

    public void decreaseStocks() {
        int remainingQuantity = decreasePromotionAmount(quantity);
        decreaseNormalAmount(remainingQuantity);
    }

    public int getProductPrice() {
        return products.getFirst().getPrice();
    }

    public int getTotalAvailableQuantity() {
        return products.stream().mapToInt(Product::getQuantity).sum();
    }

    public int getPromotionStock() {
        return products.stream().filter(each -> each.promotionExists(orderDate)).mapToInt(Product::getQuantity).sum();
    }

    public String getProductName() {
        return products.getFirst().getName();
    }

    public int getTotalPrice() {
        return getProductPrice() * quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    private int decreasePromotionAmount(int quantity) {
        Optional<Product> promotionProduct = getPromotionProduct();
        return promotionProduct.map(product -> product.decrease(quantity)).orElse(quantity);
    }

    private void decreaseNormalAmount(int remainingQuantity){
        getNormalProduct().ifPresent(product -> product.decrease(remainingQuantity));
    }

    private Optional<Product> getPromotionProduct() {
        return products.stream().filter(each -> each.promotionExists(orderDate)).findFirst();
    }

    private Optional<Product> getNormalProduct() {
        return products.stream().filter(product -> !product.promotionExists(orderDate)).findFirst();
    }

    private int getNormalProductQuantity() {
        return Math.max(quantity - getPromotionStock(), 0);
    }
}
