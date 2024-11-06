package store.infra.loader;

import java.util.List;
import store.domain.Product;
import store.domain.Promotion;
import store.infra.PromotionFactory;

public class ProductLoader extends FileLoader<Product> {
    private static final int EXPECTED_FIELD_COUNT = 4;
    private static final String FILE_PATH = "src/main/resources/products.md";

    private final PromotionFactory promotionFactory;

    public ProductLoader(PromotionFactory promotionFactory) {
        this.promotionFactory = promotionFactory;
    }

    @Override
    protected List<Product> load(String filePath) {
        List<String> lines = readFileLines(filePath);
        return parseLines(lines);
    }

    public List<Product> loadProducts() {
        return load(FILE_PATH);
    }

    @Override
    protected List<Product> parseLines(List<String> lines) {
        return lines.stream()
                .skip(1) // Skip header
                .map(this::createProduct)
                .toList();
    }

    private Product createProduct(String line) {
        String[] fields = getDeclaredFields(line, EXPECTED_FIELD_COUNT);
        String name = fields[0].trim();
        int price = parseInteger(fields[1]);
        int quantity = parseInteger(fields[2]);
        String promotionName = fields[3].trim();
        Promotion promotion = promotionFactory.getPromotion(promotionName);
        return new Product(name, price, quantity, promotion);
    }
}
