package store.presentation;

import java.util.List;
import store.domain.Orders;

public class ReceiptFormatter {
    private static final String STORE_HEADER = "==============W 편의점================";
    private static final String PROMOTION_HEADER = "=============증\t\t정===============";
    private static final String DIVIDER = "====================================";

    private final Orders orders;
    private final int memberShipDiscount;

    public ReceiptFormatter(Orders orders, int memberShipDiscount) {
        this.orders = orders;
        this.memberShipDiscount = memberShipDiscount;
    }

    public String format() {
        StringBuilder receipt = new StringBuilder();
        appendOrderSection(receipt);
        appendPromotionSection(receipt);
        appendPaymentSection(receipt);
        return receipt.toString();
    }

    private void appendOrderSection(StringBuilder receipt) {
        receipt.append(STORE_HEADER).append(System.lineSeparator());
        formatReceipts(receipt, Receipt.ofList(orders));
    }

    private void appendPromotionSection(StringBuilder receipt) {
        receipt.append(PROMOTION_HEADER).append(System.lineSeparator());
        formatReceipts(receipt, Receipt.ofPromotedOrders(orders));
        receipt.append(DIVIDER).append(System.lineSeparator());
    }

    private void appendPaymentSection(StringBuilder receipt) {
        PaymentDetail paymentDetail = PaymentDetail.of(orders, memberShipDiscount);
        appendPaymentLine(receipt, "총구매액", "", paymentDetail.totalAmount());
        appendPaymentLine(receipt, "행사할인", "-", paymentDetail.promotionDiscount());
        appendPaymentLine(receipt, "멤버십할인", "-", memberShipDiscount);
        appendPaymentLine(receipt, "내실돈", "", paymentDetail.paymentAmount());
    }

    private void formatReceipts(StringBuilder receipt, List<Receipt> receipts) {
        receipts.forEach(item -> appendReceiptLine(receipt, item));
    }

    private void appendReceiptLine(StringBuilder receipt, Receipt item) {
        receipt.append(item.format()).append(System.lineSeparator());
    }

    private void appendPaymentLine(StringBuilder receipt, String title, String prefix, int amount) {
        receipt.append(String.format("%s\t\t%s\t%,10d%s", title, prefix, amount, System.lineSeparator()));
    }
}
