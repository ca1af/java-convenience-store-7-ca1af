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

    private static boolean findProductByName(String productName, Product each) {
        return each.getName().equals(productName);
    }

    public List<Product> findAllByName(String productName) {
        return stocks.stream()
                .filter(each -> findProductByName(productName, each))
                .toList();
    }

    @Override
    public String toString() {
        return stocks.stream()
                .map(Product::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
