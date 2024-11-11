package store.application;

import java.util.List;
import store.domain.Order;

public class ReceiptFormatter {
    private static final String STORE_HEADER = "==============W 편의점================";
    private static final String COLUMN_HEADER = "상품명\t\t수량\t금액";
    private static final String PROMOTION_HEADER = "=============증\t\t정===============";
    private static final String DIVIDER = "====================================";

    private final Order order;
    private final int memberShipDiscount;

    public ReceiptFormatter(Order order, int memberShipDiscount) {
        this.order = order;
        this.memberShipDiscount = memberShipDiscount;
    }

    public String format() {
        return String.join(
                System.lineSeparator(),
                formatOrderSection(),
                formatPromotionSection(),
                formatPaymentSection()
        );
    }

    private String formatOrderSection() {
        List<Receipt> receipts = Receipt.ofList(order);
        return String.join(
                System.lineSeparator(),
                STORE_HEADER,
                COLUMN_HEADER,
                formatReceipts(receipts)
        );
    }

    private String formatPromotionSection() {
        List<Receipt> promotedReceipts = Receipt.ofPromotedOrders(order);
        return String.join(
                System.lineSeparator(),
                PROMOTION_HEADER,
                formatReceipts(promotedReceipts),
                DIVIDER
        );
    }

    private String formatPaymentSection() {
        PaymentDetail paymentDetail = PaymentDetail.of(order, memberShipDiscount);
        return String.join(
                System.lineSeparator(),
                formatPaymentLine("총구매액", formatMoney(paymentDetail.totalAmount())),
                formatPaymentLine("행사할인", formatDiscount(paymentDetail.promotionDiscount())),
                formatPaymentLine("멤버십할인", formatDiscount(memberShipDiscount)),
                formatPaymentLine("내실돈", formatMoney(paymentDetail.paymentAmount()))
        );
    }

    private String formatReceipts(List<Receipt> receipts) {
        return receipts.stream()
                .map(Receipt::format)
                .reduce((a, b) -> a + System.lineSeparator() + b)
                .orElse("");
    }

    private String formatPaymentLine(String title, String amount) {
        return String.format("%s\t\t%s", title, amount);
    }

    private String formatDiscount(int discount) {
        return String.format("-%,d", discount);
    }

    private String formatMoney(int amount){
        return String.format("%,d", amount);
    }
}
