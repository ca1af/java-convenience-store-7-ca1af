package store.domain;

import java.util.List;

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
        boolean hasDifferentProducts = stocks.stream()
                .anyMatch(product -> !product.getName().equals(productName));

        if (hasDifferentProducts) {
            throw new IllegalArgumentException(DomainErrorMessage.NOT_IDENTICAL.getMessage());
        }
    }

    public int getMaxCount(){
        return stocks.stream()
                .mapToInt(Product::getQuantity)
                .sum();
    }

    public boolean hasRemain(int quantity){
        return stocks.stream().anyMatch(each -> each.hasRemains(quantity));
    }

    public String getProductName(){
        return stocks.getFirst().getName();
    }

    public int getPromotionStock() {
        return stocks.stream()
                .filter(Product::promotionExists)
                .mapToInt(Product::getQuantity)
                .sum();
    }
}
