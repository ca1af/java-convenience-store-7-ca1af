package store.domain.discount;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import store.domain.DomainErrorMessage;

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
    void testHasUnclaimedFreeItem(String name, int buy, int get, int quantity, boolean expected) {
        LocalDate today = LocalDate.now();
        Promotion promotion = new Promotion(name, buy, get, today.minusDays(1).atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        boolean result = promotion.hasUnclaimedFreeItem(quantity);

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
        Promotion promotion = new Promotion("foo", 2, 1, start.atStartOfDay(), end.atStartOfDay());

        boolean result = promotion.applicable(now.atStartOfDay());

        Assertions.assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("현재 날짜가 프로모션 기간에 해당하는지 확인한다")
    @CsvSource({
            "2024-01-01, 2024-12-31",   // 기간 내
            "2024-01-01, 2024-12-31",  // 종료 후
            "2024-01-01, 2024-12-31",  // 시작 전
            "2024-01-01, 2024-12-31",   // 시작일
            "2024-01-01, 2024-12-31"    // 종료일
    })
    void invalidDate(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atStartOfDay();
        assertThatThrownBy(() -> new Promotion("foo", 2, 1,end, start))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DomainErrorMessage.INVALID_PROMOTION_DATE.getMessage());
    }
}
