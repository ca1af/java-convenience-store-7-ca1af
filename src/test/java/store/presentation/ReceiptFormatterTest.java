package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import store.domain.Order;
import store.domain.Orders;
import store.domain.Product;
import store.domain.Promotion;
import store.application.ReceiptFormatter;

class ReceiptFormatterTest {
    private LocalDateTime orderDate;

    @BeforeEach
    void setUp() {
        orderDate = DateTimes.now();
    }

    @Test
    @DisplayName("영수증 출력 테스트 - 정상 출력")
    void formatReceiptSuccessfully() {
        // given
        Product cola = new Product("콜라", 1000, 10, new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));
        Product energyBar = new Product("에너지바", 2000, 5, null);

        Order order1 = new Order(List.of(cola), 6, orderDate); // 6개 구매 -> 2+1 프로모션 -> 증정 2개
        Order order2 = new Order(List.of(energyBar), 5, orderDate); // 5개 구매, 프로모션 없음

        Orders orders = new Orders(List.of(order1, order2));
        int memberShipDiscount = 3000;

        ReceiptFormatter printer = new ReceiptFormatter(orders, memberShipDiscount);

        // when
        String receipt = printer.format();

        // then
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

    @Test
    @DisplayName("영수증 출력 테스트 - 프로모션 없는 경우")
    void formatReceiptWithoutPromotion() {
        // given
        Product water = new Product("물", 500, 10, null);
        Product snack = new Product("스낵", 1500, 5, null);

        Order order1 = new Order(List.of(water), 3, orderDate); // 3개 구매
        Order order2 = new Order(List.of(snack), 4, orderDate); // 4개 구매

        Orders orders = new Orders(List.of(order1, order2));
        int memberShipDiscount = 2000;

        ReceiptFormatter printer = new ReceiptFormatter(orders, memberShipDiscount);

        // when
        String receipt = printer.format();

        // then
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

    @Test
    @DisplayName("영수증 출력 테스트 - 멤버십 할인 없는 경우")
    void formatReceiptWithoutMemberShipDiscount() {
        // given
        Product juice = new Product("주스", 2000, 10, new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));

        Order order = new Order(List.of(juice), 8, orderDate); // 8개 구매 -> 1+1 프로모션 -> 증정 4개

        Orders orders = new Orders(List.of(order));
        int memberShipDiscount = 0;

        ReceiptFormatter printer = new ReceiptFormatter(orders, memberShipDiscount);

        // when
        String receipt = printer.format();

        // then
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
}
