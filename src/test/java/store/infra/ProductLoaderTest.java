package store.infra;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.Product;
import store.domain.Promotion;

class ProductLoaderTest {
    private ProductLoader productLoader;

    @BeforeEach
    void setUp() {
        PromotionLoader promotionLoader = new PromotionLoader();
        List<Promotion> promotions = promotionLoader.loadPromotions();
        PromotionFactory promotionFactory = new PromotionFactory(promotions);
        productLoader = new ProductLoader(promotionFactory);
    }

    @DisplayName("상품을 파일에서 불러와 올바르게 초기화할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "콜라,1000,10,탄산2+1",
            "오렌지주스,1800,9,MD추천상품",
            "감자칩,1500,5,반짝할인"
    })
    void verifyProductDetailsWithPromotions(String name, int price, int quantity, String promotionName) throws IOException {
        List<Product> products = productLoader.loadProducts();

        Product product = findProductByNameAndPromotion(products, name, promotionName);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.name()).isEqualTo(name);
            softly.assertThat(product.price()).isEqualTo(price);
            softly.assertThat(product.quantity()).isEqualTo(quantity);
            softly.assertThat(product.promotion().name()).isEqualTo(promotionName);
        });
    }

    @DisplayName("상품을 파일에서 불러와 프로모션 없이 초기화할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "콜라,1000,10,null",
            "비타민워터,1500,6,null",
            "정식도시락,6400,8,null"
    })
    void verifyProductDetailsWithoutPromotions(String name, int price, int quantity, String promotionName) throws IOException {
        List<Product> products = productLoader.loadProducts();

        Product product = findProductByNameAndPromotion(products, name, promotionName);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.name()).isEqualTo(name);
            softly.assertThat(product.price()).isEqualTo(price);
            softly.assertThat(product.quantity()).isEqualTo(quantity);
            softly.assertThat(product.promotion()).isNull();
        });
    }

    private Product findProductByNameAndPromotion(List<Product> products, String name, String promotionName) {
        return products.stream()
                .filter(p -> exists(name, promotionName, p))
                .findFirst()
                .orElseThrow();
    }

    private static boolean exists(String name, String promotionName, Product p) {
        if (!p.name().equals(name)){
            return false;
        }

        if (Objects.isNull(p.promotion())){
            return Objects.equals(promotionName, "null");
        }

        return p.promotion().name().equals(promotionName);
    }
}
