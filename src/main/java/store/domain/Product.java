package store.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Product {
    private final String name;
    private final int price;
    private int quantity;
    private final Promotion promotion;

    public Product(String name, int price, int quantity, Promotion promotion) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotion = promotion;
    }

    public int decrease(int purchaseQuantity) {
        if (quantity >= purchaseQuantity) {
            quantity -= purchaseQuantity;
            return 0;
        }

        int remaining = purchaseQuantity - quantity;
        quantity = 0;
        return remaining;
    }

    public int getPromotedCount(int orderQuantity){
        if (orderQuantity > quantity){
            return promotion.promotionGetCount(quantity);
        }
        return promotion.promotionGetCount(orderQuantity);
    }

    public boolean promotionExists(LocalDateTime orderDate) {
        return promotionNotNull() && promotion.applicable(orderDate);
    }

    private boolean promotionNotNull(){
        return !Objects.isNull(promotion);
    }

    public boolean hasUnclaimedFreeItem(int quantity, LocalDateTime orderDate) {
        if (!promotionExists(orderDate) || quantity <= 0){
            return false;
        }
        if (quantity >= this.quantity) { // 같다면 무료 증정이 불가하다 (1+1 으로 5개 주문, 재고 5개면 5개 나가야한다.)
            return false;
        }
        return promotion.hasUnclaimedFreeItem(quantity);
    }

    @Override
    public String toString() {
        if (quantity > 0){
            return formatWithStock();
        }

        return formatOutOfStock();
    }

    private String formatWithStock() {
        StringBuilder sb = new StringBuilder();
        appendBasicInfo(sb);
        appendPromotionInfo(sb);
        return sb.toString();
    }

    private String formatOutOfStock() {
        StringBuilder sb = new StringBuilder("- ")
                .append(name)
                .append(" ")
                .append(String.format("%,d원", price))
                .append(" 재고 없음");
        appendPromotionInfo(sb);
        return sb.toString();
    }

    private void appendBasicInfo(StringBuilder sb) {
        sb.append("- ")
                .append(name)
                .append(" ")
                .append(String.format("%,d원", price))
                .append(" ")
                .append(quantity)
                .append("개");
    }

    private void appendPromotionInfo(StringBuilder sb) {
        if (promotionNotNull()) {
            sb.append(" ").append(promotion.name());
        }
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice(){
        return price;
    }
}
