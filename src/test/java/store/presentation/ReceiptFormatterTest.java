package store.presentation;

import camp.nextstep.edu.missionutils.DateTimes;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.domain.OrderProduct;
import store.domain.Order;
import store.domain.Product;
import store.domain.Promotion;

class ReceiptFormatterTest {
    private LocalDateTime orderDate;

    @BeforeEach
    void setUp() {
        orderDate = DateTimes.now();
    }

    @Test
    @DisplayName("영수증 출력 테스트 - 정상 출력")
    void formatReceiptSuccessfully() {
        Product cola = new Product("콜라", 1000, 10,
                new Promotion("2+1", 2, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));
        Product energyBar = new Product("에너지바", 2000, 5, null);

        OrderProduct orderProduct1 = new OrderProduct(List.of(cola), 6, orderDate); // 6개 구매 -> 2+1 프로모션 -> 증정 2개
        OrderProduct orderProduct2 = new OrderProduct(List.of(energyBar), 5, orderDate); // 5개 구매, 프로모션 없음

        Order order = new Order(List.of(orderProduct1, orderProduct2));
        int memberShipDiscount = 3000;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        String receipt = printer.format();

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

        Product water = new Product("물", 500, 10, null);
        Product snack = new Product("스낵", 1500, 5, null);

        OrderProduct orderProduct1 = new OrderProduct(List.of(water), 3, orderDate); // 3개 구매
        OrderProduct orderProduct2 = new OrderProduct(List.of(snack), 4, orderDate); // 4개 구매

        Order order = new Order(List.of(orderProduct1, orderProduct2));
        int memberShipDiscount = 2000;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        String receipt = printer.format();

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

        Product juice = new Product("주스", 2000, 10,
                new Promotion("1+1", 1, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));

        OrderProduct orderProduct = new OrderProduct(List.of(juice), 8, orderDate); // 8개 구매 -> 1+1 프로모션 -> 증정 4개

        Order order = new Order(List.of(orderProduct));
        int memberShipDiscount = 0;

        ReceiptFormatter printer = new ReceiptFormatter(order, memberShipDiscount);

        String receipt = printer.format();

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
