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
        return !Objects.isNull(promotion);
    }

    public boolean hasRemains(int quantity) {
        return promotion.hasFreeRemains(quantity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendBasicInfo(sb);
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
        if (promotionExists()) {
            sb.append(" ").append(promotion.name());
        }
    }

    public int getTotalPrice() {
        return price * quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public Promotion getPromotion() {
        return promotion;
    }
}
