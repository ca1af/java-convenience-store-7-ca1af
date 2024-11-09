package store.infra.loader;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.Promotion;

class PromotionLoaderTest {
    private final PromotionLoader promotionLoader = new PromotionLoader();

    @DisplayName("프로모션을 불러오는 데 성공한다.")
    @ParameterizedTest
    @CsvSource({
            "탄산2+1,2,1,2024-01-01,2024-12-31",
            "MD추천상품,1,1,2024-01-01,2024-12-31",
            "반짝할인,1,1,2024-11-01,2024-11-30"
    })
    void verifyPromotionDetails(String name, int buyQuantity, int getQuantity, String startDate, String endDate) {
        List<Promotion> promotions = promotionLoader.loadPromotions();

        Promotion promotion = findPromotionByName(promotions, name);

        SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(promotion.name()).isEqualTo(name);
                    softly.assertThat(promotion.buy()).isEqualTo(buyQuantity);
                    softly.assertThat(promotion.get()).isEqualTo(getQuantity);
                    softly.assertThat(promotion.startDate()).isEqualTo(LocalDate.parse(startDate).atStartOfDay());
                    softly.assertThat(promotion.endDate()).isEqualTo(LocalDate.parse(endDate).atStartOfDay());
                    softly.assertAll();
                }
        );
    }

    private Promotion findPromotionByName(List<Promotion> promotions, String name) {
        return promotions.stream()
                .filter(p -> p.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Promotion not found: " + name));
    }
}
