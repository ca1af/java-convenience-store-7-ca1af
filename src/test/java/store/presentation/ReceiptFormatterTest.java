package store.presentation;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.application.ReceiptFormatter;
import store.domain.discount.Promotion;
import store.domain.order.NormalOrderItem;
import store.domain.order.Order;
import store.domain.order.PromotionOrderItem;
import store.domain.product.NormalProduct;
import store.domain.product.PromotionProduct;

class ReceiptFormatterTest {
    private LocalDateTime orderDate;

    @BeforeEach
    void setUp() {
        orderDate = LocalDateTime.now();
    }

    @Test
    @DisplayName("영수증 출력 테스트 - 정상 출력")
    void formatReceiptSuccessfully() {
        Promotion promotion = new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        String receipt = getReceipt(promotion);

        String expectedReceipt = """
                ==============W 편의점================
                콜라      	 6	  6,000
                에너지바    	 5	 10,000
                =============증		정===============
                콜라      	 2
                ====================================
                총구매액		    	 16,000
                행사할인		-	  2,000
                멤버십할인		-	  3,000
                내실돈		    	 11,000
                """;
        Assertions.assertThat(receipt.replace(" ", "")).isEqualTo(expectedReceipt.replace(" ", ""));
    }

    private String getReceipt(Promotion promotion) {
        PromotionProduct colaPromoProduct = new PromotionProduct("콜라", 1000, 10, promotion);
        NormalProduct colaNormalProduct = new NormalProduct("콜라", 1000, 10);
        NormalProduct energyBar = new NormalProduct("에너지바", 2000, 5);

        PromotionOrderItem orderProduct1 = new PromotionOrderItem(colaNormalProduct, colaPromoProduct, 6, orderDate); // 6개 구매 -> 2+1 프로모션 -> 증정 2개
        NormalOrderItem orderProduct2 = new NormalOrderItem(energyBar, 5); // 5개 구매, 프로모션 없음

        Order order = new Order(List.of(orderProduct1, orderProduct2));
        int memberShipDiscount = 3000;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        return printer.format();
    }

    @Test
    @DisplayName("영수증 출력 테스트 - 프로모션 없는 경우")
    void formatReceiptWithoutPromotion() {

        String receipt = getNormalReceipt();

        String expectedReceipt = """
                ==============W 편의점================
                물      	 3	  1,500
                스낵      	 4	  6,000
                =============증		정===============
                ====================================
                총구매액		    	  7,500
                행사할인		-	      0
                멤버십할인		-	  2,000
                내실돈		    	  5,500
                """;
        Assertions.assertThat(receipt.replace(" ", "")).isEqualTo(expectedReceipt.replace(" ", ""));
    }

    private static String getNormalReceipt() {
        NormalProduct water = new NormalProduct("물", 500, 10);
        NormalProduct snack = new NormalProduct("스낵", 1500, 5);

        NormalOrderItem orderProduct1 = new NormalOrderItem(water, 3); // 3개 구매
        NormalOrderItem orderProduct2 = new NormalOrderItem(snack, 4); // 4개 구매

        Order order = new Order(List.of(orderProduct1, orderProduct2));
        int memberShipDiscount = 2000;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        return printer.format();
    }

    @Test
    @DisplayName("영수증 출력 테스트 - 멤버십 할인 없는 경우")
    void formatReceiptWithoutMemberShipDiscount() {

        Promotion promotion = new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        String receipt = getPromotionReceipt(promotion);

        String expectedReceipt = """
                ==============W 편의점================
                주스      	 8	 16,000
                =============증		정===============
                주스      	 4
                ====================================
                총구매액		    	 16,000
                행사할인		-	  8,000
                멤버십할인		-	      0
                내실돈		    	  8,000
                """;
        Assertions.assertThat(receipt.replace(" ", "")).isEqualTo(expectedReceipt.replace(" ", ""));
    }

    private String getPromotionReceipt(Promotion promotion) {
        PromotionProduct juicePromoProduct = new PromotionProduct("주스", 2000, 10, promotion);
        NormalProduct juiceNormalProduct = new NormalProduct("주스", 2000, 10);

        PromotionOrderItem orderProduct = new PromotionOrderItem(juiceNormalProduct, juicePromoProduct, 8, orderDate); // 8개 구매 -> 1+1 프로모션 -> 증정 4개

        Order order = new Order(List.of(orderProduct));
        int memberShipDiscount = 0;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        return printer.format();
    }
}
