package store.infra;

import store.infra.loader.ProductLoader;
import store.infra.loader.PromotionLoader;

public class FileLoaderProductRepository {
    private final ProductStorage productStorage;

    public FileLoaderProductRepository() {
        PromotionLoader promotionLoader = new PromotionLoader();
        PromotionFactory promotionFactory = new PromotionFactory(promotionLoader.loadPromotions());
        ProductLoader productLoader = new ProductLoader(promotionFactory);
        this.productStorage = new ProductStorage(productLoader.loadProducts());
    }

    public ProductStorage findAllByName(String productName) {
        if (!productStorage.hasProduct(productName)) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PRODUCT_NAME.getMessage());
        }

        return new ProductStorage(productStorage.findAllByName(productName));
    }

    public ProductStorage findAll(){
        return productStorage;
    }

    public boolean hasEnoughQuantity(String productName, int quantity) {
        return productStorage.hasQuantity(productName, quantity);
    }

    public boolean exists(String productName) {
        return productStorage.exists(productName);
    }
}
