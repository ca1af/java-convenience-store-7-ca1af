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
        if (!stocks.hasProduct(productName)) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PRODUCT_NAME.getMessage());
        }

        return stocks.findAllByName(productName);
    }

    public Products findAll(){
        return stocks;
    }

    public boolean hasEnoughQuantity(String productName, int quantity) {
        return stocks.hasQuantity(productName, quantity);
    }

    public boolean exists(String productName) {
        return stocks.exists(productName);
    }
}
