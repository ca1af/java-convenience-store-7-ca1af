package store.infra;

import java.util.List;
import store.domain.Product;
import store.infra.loader.ProductLoader;
import store.infra.loader.PromotionLoader;

public class FileLoaderProductRepository {
    private final Products stocks;

    public FileLoaderProductRepository() {
        PromotionLoader promotionLoader = new PromotionLoader();
        PromotionFactory promotionFactory = new PromotionFactory(promotionLoader.loadPromotions());
        ProductLoader productLoader = new ProductLoader(promotionFactory);
        this.stocks = new Products(productLoader.loadProducts());
    }

    public List<Product> findAllByName(String productName) {
        return stocks.findAllByName(productName);
    }

    public void deleteProducts(List<Product> purchasedProducts) {
        stocks.delete(purchasedProducts);
    }
}
