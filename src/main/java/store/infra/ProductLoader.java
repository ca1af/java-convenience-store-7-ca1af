package store.infra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import store.domain.Product;
import store.domain.Promotion;

public class ProductLoader {
    private static final String FILE_PATH = "src/main/resources/products.md";
    private static final String DELIMITER = ",";
    private static final int EXPECTED_FIELD_COUNT = 4;

    private final PromotionFactory promotionFactory;

    public ProductLoader(PromotionFactory promotionFactory) {
        this.promotionFactory = promotionFactory;
    }

    public List<Product> loadProducts() throws IOException {
        List<String> lines = readFileLines();
        return parseProducts(lines);
    }

    private List<String> readFileLines() throws IOException {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(InfraErrorMessage.FILE_NOT_FOUND.getMessage());
        }
        return Files.readAllLines(path);
    }

    private List<Product> parseProducts(List<String> lines) {
        return lines.stream()
                .skip(1) // Skip header
                .map(this::createProduct)
                .toList();
    }

    private Product createProduct(String line) {
        String[] fields = line.split(DELIMITER);
        if (fields.length != EXPECTED_FIELD_COUNT) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_PRODUCT_FORMAT.getMessage());
        }

        return loadProduct(fields);
    }

    private Product loadProduct(String[] fields) {
        String name = fields[0].trim();
        int price = parseInteger(fields[1]);
        int quantity = parseInteger(fields[2]);
        String promotionName = fields[3].trim();
        Promotion promotion = promotionFactory.getPromotion(promotionName);
        return new Product(name, price, quantity, promotion);
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(InfraErrorMessage.INVALID_INTEGER.getMessage());
        }
    }
}
