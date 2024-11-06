package store.infra;

import java.util.List;
import store.domain.Product;

public class Products {
    private final List<Product> stocks;

    public Products(List<Product> stocks) {
        this.stocks = stocks; // 불변이어서는 안된다.
    }

    public List<Product> findAllByName(String productName) {
        return stocks.stream()
                .filter(each -> each.name().equals(productName))
                .toList();
    }

    public void delete(List<Product> products){
        stocks.removeAll(products);
    }
}
