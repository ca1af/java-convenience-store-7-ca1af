package store.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Order {
    private final List<Product> stocks;
    private int quantity;
    private final LocalDateTime orderDate;

    public Order(List<Product> stocks, int quantity, LocalDateTime orderDate) {
        this.orderDate = orderDate;
        validateDifferentProducts(stocks);
        this.stocks = stocks;
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(DomainErrorMessage.INVALID_QUANTITY.getMessage());
        }
    }

    private static void validateDifferentProducts(List<Product> stocks) {
        String productName = stocks.getFirst().getName();
        boolean hasDifferentProducts = stocks.stream().anyMatch(product -> !product.getName().equals(productName));
        if (hasDifferentProducts) {
            throw new IllegalArgumentException(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    public int getProductPrice(){
        return stocks.getFirst().getPrice();
    }

    public void addQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity(int decreaseAmount) {
        this.quantity -= decreaseAmount;
    }

    public int countFallbackToNormal() {
        int promotionStock = getPromotionStock();
        return Math.max(quantity - promotionStock, 0);
    }

    public int getNormalProductPrice() {
        Optional<Product> normalProduct = getNormalProduct();
        return normalProduct.map(product -> product.getPrice() * getNormalProductQuantity()).orElse(0);
    }

    public int getPromotedCount() {
        Optional<Product> promotionProduct = getPromotionProduct();
        return promotionProduct.map(product -> product.getPromotedCount(quantity)).orElse(0);
    }

    public boolean hasUnclaimedFreeItem() {
        return stocks.stream().anyMatch(product -> product.hasUnclaimedFreeItem(quantity, orderDate));
    }

    public boolean hasFallbackToNormal() {
        return quantity > getPromotionStock();
    }

    public boolean available() {
        return getMaxCount() >= quantity;
    }

    public int getMaxCount() {
        return stocks.stream().mapToInt(Product::getQuantity).sum();
    }

    public int getPromotionStock() {
        return stocks.stream().filter(each -> each.promotionExists(orderDate)).mapToInt(Product::getQuantity).sum();
    }

    public String getProductName() {
        return stocks.getFirst().getName();
    }

    public void decreaseStocks() {
        int remainingQuantity = decreasePromotionAmount(quantity);
        getNormalProduct().ifPresent(product -> product.decrease(remainingQuantity));
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

    private Optional<Product> getPromotionProduct() {
        return stocks.stream().filter(each -> each.promotionExists(orderDate)).findFirst();
    }

    private Optional<Product> getNormalProduct() {
        return stocks.stream().filter(product -> !product.promotionExists(orderDate)).findFirst();
    }

    private int getNormalProductQuantity() {
        return Math.max(quantity - getPromotionStock(), 0);
    }
}
