package store.infra.loader;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.Product;
import store.domain.Promotion;
import store.infra.PromotionFactory;

class ProductLoaderTest {
    private ProductLoader productLoader;

    @BeforeEach
    void setUp() {
        PromotionLoader promotionLoader = new PromotionLoader();
        List<Promotion> promotions = promotionLoader.loadPromotions();
        PromotionFactory promotionFactory = new PromotionFactory(promotions);
        productLoader = new ProductLoader(promotionFactory);
    }

    @ParameterizedTest
    @CsvSource({
            "콜라,1000,10,탄산2+1",
            "오렌지주스,1800,9,MD추천상품",
            "감자칩,1500,5,반짝할인"
    })
    @DisplayName("파일에서 상품을 올바르게 로드하고 프로모션을 적용할 수 있다")
    void shouldLoadProductsWithPromotion(String name, int price, int quantity, String promotionName) {
        // given
        List<Product> products = productLoader.loadProducts();

        // when
        Product product = findProductByNameAndPromotion(products, name, promotionName);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.getName()).isEqualTo(name);
            softly.assertThat(product.getPrice()).isEqualTo(price);
            softly.assertThat(product.getQuantity()).isEqualTo(quantity);
            softly.assertThat(product.promotionExists(DateTimes.now())).isTrue();
        });
    }

    @ParameterizedTest
    @CsvSource({
            "콜라,1000,10,null",
            "비타민워터,1500,6,null",
            "정식도시락,6400,8,null"
    })
    @DisplayName("파일에서 프로모션이 없는 상품을 올바르게 로드할 수 있다")
    void shouldLoadProductsWithoutPromotion(String name, int price, int quantity, String promotionName) {
        // given
        List<Product> products = productLoader.loadProducts();

        // when
        Product product = findProductByNameAndPromotion(products, name, promotionName);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.getName()).isEqualTo(name);
            softly.assertThat(product.getPrice()).isEqualTo(price);
            softly.assertThat(product.getQuantity()).isEqualTo(quantity);
            softly.assertThat(product.promotionExists(DateTimes.now())).isFalse();
        });
    }

    private Product findProductByNameAndPromotion(List<Product> products, String name, String promotionName) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .filter(product ->
                        ("null".equals(promotionName) && !product.promotionExists(DateTimes.now())) ||
                                (!"null".equals(promotionName) && product.promotionExists(DateTimes.now()))
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching product found."));
    }
}
