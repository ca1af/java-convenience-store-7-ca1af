package store.infra.loader;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.discount.Promotion;
import store.domain.product.NormalProduct;
import store.domain.product.Product;
import store.domain.product.PromotionProduct;
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
    @CsvSource({"콜라,1000,10,탄산2+1", "오렌지주스,1800,9,MD추천상품", "감자칩,1500,5,반짝할인"})
    @DisplayName("파일에서 상품을 올바르게 로드하고 프로모션을 적용할 수 있다")
    void shouldLoadProductsWithPromotion(String name, int price, int quantity) {
        List<Product> products = productLoader.loadProducts();

        Product product = findProductByNameAndPromotion(products, name);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.getName()).isEqualTo(name);
            softly.assertThat(product.getPrice()).isEqualTo(price);
            softly.assertThat(product.getQuantity()).isEqualTo(quantity);
            softly.assertThat(product).isInstanceOf(PromotionProduct.class);
        });
    }

    @ParameterizedTest
    @CsvSource({"물,500,10,null", "비타민워터,1500,6,null", "정식도시락,6400,8,null"})
    @DisplayName("파일에서 프로모션이 없는 상품을 올바르게 로드할 수 있다")
    void shouldLoadProductsWithoutPromotion(String name, int price, int quantity) {
        List<Product> products = productLoader.loadProducts();

        Product product = findProductByNameAndPromotion(products, name);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(product.getName()).isEqualTo(name);
            softly.assertThat(product.getPrice()).isEqualTo(price);
            softly.assertThat(product.getQuantity()).isEqualTo(quantity);
            softly.assertThat(product).isInstanceOf(NormalProduct.class);
        });
    }

    private Product findProductByNameAndPromotion(List<Product> products, String name) {
        return products.stream().filter(product -> {
            if (product instanceof PromotionProduct) {
                return product.getName().equals(name);
            }
            return product.getName().equals(name);
        }).findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
