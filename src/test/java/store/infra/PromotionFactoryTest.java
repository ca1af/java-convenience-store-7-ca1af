package store.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.Promotion;

class PromotionFactoryTest {
    private PromotionFactory promotionFactory;

    @BeforeEach
    void setUp() {
        List<Promotion> promotions = List.of(
                new Promotion("탄산2+1", 2, 1, LocalDate.of(2024, 1, 1).atStartOfDay(), LocalDate.of(2024, 12, 31).atStartOfDay()),
                new Promotion("MD추천상품", 1, 1, LocalDate.of(2024, 1, 1).atStartOfDay(), LocalDate.of(2024, 12, 31).atStartOfDay()),
                new Promotion("반짝할인", 1, 1, LocalDate.of(2024, 11, 1).atStartOfDay(), LocalDate.of(2024, 11, 30).atStartOfDay())
        );
        promotionFactory = new PromotionFactory(promotions);
    }

    @DisplayName("name 에 해당하는 프로모션 객체를 가져 올 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "탄산2+1, 2, 1, 2024-01-01, 2024-12-31",
            "MD추천상품, 1, 1, 2024-01-01, 2024-12-31",
            "반짝할인, 1, 1, 2024-11-01, 2024-11-30"
    })
    void shouldReturnCorrectPromotion_WhenValidNameProvided(String name, int buyQuantity, int getQuantity, String startDate, String endDate) {
        Promotion promotion = promotionFactory.getPromotion(name);

        SoftAssertions.assertSoftly(
                softly -> {
                    assertThat(promotion).isNotNull();
                    assertThat(promotion.name()).isEqualTo(name);
                    assertThat(promotion.buy()).isEqualTo(buyQuantity);
                    assertThat(promotion.get()).isEqualTo(getQuantity);
                    assertThat(promotion.startDate()).isEqualTo(LocalDate.parse(startDate).atStartOfDay());
                    assertThat(promotion.endDate()).isEqualTo(LocalDate.parse(endDate).atStartOfDay());
                }
        );
    }

    @DisplayName("없는 프로모션 이름으로 조회할 때 예외를 발생시킨다")
    @Test
    void shouldReturnNull_WhenInvalidNameProvided() {
        assertThatThrownBy(() -> promotionFactory.getPromotion("없는프로모션"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InfraErrorMessage.INVALID_PROMOTION_NAME.getMessage());
    }
}
