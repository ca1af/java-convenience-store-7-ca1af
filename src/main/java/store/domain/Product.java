package store.domain;

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

    public boolean promotionExists() {
        return !Objects.isNull(promotion) && quantity > 0;
    }

    public boolean hasRemains(int quantity) {
        if (!promotionExists()){
            return false;
        }
        return promotion.hasFreeRemains(quantity);
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
        return "- "
                + name
                + " "
                + String.format("%,d원", price)
                + " 재고 없음";
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
        if (promotionExists()) {
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
