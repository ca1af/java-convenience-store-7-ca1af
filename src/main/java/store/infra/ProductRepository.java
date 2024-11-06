package store.infra;

import java.util.List;
import store.domain.Product;
import store.infra.loader.ProductLoader;

public class ProductRepository {
    private final Products stocks;

    private ProductRepository(Products stocks) {
        this.stocks = stocks;
    }

    public static ProductRepository create(ProductLoader productLoader) {
        List<Product> stocks = productLoader.loadProducts();
        return new ProductRepository(new Products(stocks));
    }

    public List<Product> findAllByName(String productName) {
        return stocks.findAllByName(productName);
    }

    public void deleteProducts(List<Product> purchasedProducts) {
        stocks.delete(purchasedProducts);
    }
}
