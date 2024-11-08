package store.infra;

import java.util.List;
import store.domain.Product;

public class ProductStorage {
    private final List<Product> stocks;

    public ProductStorage(List<Product> stocks) {
        this.stocks = stocks; // 불변이어서는 안된다.
    }

    public List<Product> findAllByName(String productName) {
        return stocks.stream()
                .filter(each -> findProductByName(productName, each))
                .toList();
    }

    private static boolean findProductByName(String productName, Product each) {
        return each.getName().equals(productName);
    }

    public boolean hasProduct(String productName){
        return stocks.stream().anyMatch(each -> findProductByName(productName, each));
    }

    public boolean hasQuantity(String productName, int quantity) {
        List<Product> products = stocks.stream().filter(each -> each.getName().equals(productName)).toList();
        int availableQuantity = products.stream()
                .mapToInt(Product::getQuantity)
                .sum();

        return availableQuantity >= quantity;
    }

    public boolean exists(String productName) {
        return stocks.stream().anyMatch(each -> each.getName().equals(productName));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        stocks.forEach(sb::append);
        return sb.toString();
    }
}
