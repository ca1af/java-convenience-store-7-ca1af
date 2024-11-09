package store.infra;

import java.util.List;
import java.util.stream.Collectors;
import store.domain.Product;
import store.infra.loader.ProductLoader;
import store.infra.loader.PromotionLoader;

public class FilerLoaderProductRepository {
    private final List<Product> stocks;

    public FilerLoaderProductRepository() {
        PromotionLoader promotionLoader = new PromotionLoader();
        PromotionFactory promotionFactory = new PromotionFactory(promotionLoader.loadPromotions());
        ProductLoader productLoader = new ProductLoader(promotionFactory);
        this.stocks = productLoader.loadProducts();
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
        return stocks.stream()
                .map(Product::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
