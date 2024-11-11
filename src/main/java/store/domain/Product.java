package store.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Product {
    private static final String OUT_OF_STOCK_MESSAGE = " 재고 없음";
    private static final String WITH_STOCK_FORMAT = "%s %,d원 %d개";
    private static final String OUT_OF_STOCK_FORMAT = "%s %,d원";
    private static final String MESSAGE_PREFIX = "- ";
    private final String name;
    private final int price;
    private final Promotion promotion;
    private int quantity;

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

    public int getPromotedCount(int orderQuantity) {
        if (orderQuantity > quantity) {
            return promotion.promotionGetCount(quantity);
        }
        return promotion.promotionGetCount(orderQuantity);
    }

    public boolean promotionExists(LocalDateTime orderDate) {
        return promotionNotNull() && promotion.applicable(orderDate);
    }

    public boolean promotionNotNull() {
        return !Objects.isNull(promotion);
    }

    public boolean hasUnclaimedFreeItem(int quantity, LocalDateTime orderDate) {
        if (!promotionExists(orderDate) || quantity <= 0) {
            return false;
        }
        if (quantity >= this.quantity) { // 같다면 무료 증정이 불가하다 (1+1 으로 5개 주문, 재고 5개면 5개 나가야한다.)
            return false;
        }
        return promotion.hasUnclaimedFreeItem(quantity);
    }

    @Override
    public String toString() {
        if (quantity > 0) {
            return formatWithStock();
        }
        return formatOutOfStock();
    }

    private String formatWithStock() {
        return String.format(WITH_STOCK_FORMAT, MESSAGE_PREFIX + name, price, quantity) + getPromotionInfo();
    }

    private String formatOutOfStock() {
        return String.format(OUT_OF_STOCK_FORMAT, MESSAGE_PREFIX + name, price) + OUT_OF_STOCK_MESSAGE
                + getPromotionInfo();
    }

    private String getPromotionInfo() {
        if (promotionNotNull()) {
            return " " + promotion.name();
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}
