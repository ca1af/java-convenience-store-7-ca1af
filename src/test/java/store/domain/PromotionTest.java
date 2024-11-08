package store.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

class PromotionTest {

    @ParameterizedTest
    @DisplayName("주어진 수량에 대해 프로모션 잔여 여부를 확인한다")
    @CsvSource({
            // 1 + 1 상품들
            "1+1, 1, 1, 3, true",
            "1+1, 1, 1, 5, true",
            "1+1, 1, 1, 4, false",
            "1+1, 1, 1, 6, false",

            // 2 + 1 상품들
            "2+1, 2, 1, 8, true",
            "2+1, 2, 1, 9, false",
            "2+1, 2, 1, 7, false",

            // 3 + 1 상품들
            "3+1, 3, 1, 15, true",
            "3+1, 3, 1, 14, false"
    })
    void testHasFreeRemains(String name, int buy, int get, int quantity, boolean expected) {
        // given
        LocalDate today = LocalDate.now();
        Promotion promotion = new Promotion(name, buy, get, today.minusDays(1), today.plusDays(1));

        // when
        boolean result = promotion.hasFreeRemains(quantity);

        // then
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("현재 날짜가 프로모션 기간에 해당하는지 확인한다")
    @CsvSource({
            "2024-01-01, 2024-12-31, 2024-06-01, true",   // 기간 내
            "2024-01-01, 2024-12-31, 2025-01-01, false",  // 종료 후
            "2024-01-01, 2024-12-31, 2023-12-31, false",  // 시작 전
            "2024-01-01, 2024-12-31, 2024-01-01, true",   // 시작일
            "2024-01-01, 2024-12-31, 2024-12-31, true"    // 종료일
    })
    void testApplicable(String startDate, String endDate, String currentDate, boolean expected) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate now = LocalDate.parse(currentDate);
        Promotion promotion = new Promotion("foo", 2, 1, start, end);

        boolean result = promotion.applicable(now);

        Assertions.assertThat(result).isEqualTo(expected);
    }
}
